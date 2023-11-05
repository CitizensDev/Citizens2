package net.citizensnpcs.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.CommandMessages;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.command.exception.ServerCommandException;
import net.citizensnpcs.api.command.exception.UnhandledCommandException;
import net.citizensnpcs.api.command.exception.WrappedCommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

public class NPCCommandSelector extends NumericPrompt {
    private final Callback callback;
    private final List<NPC> choices;

    public NPCCommandSelector(Callback callback, List<NPC> possible) {
        this.callback = callback;
        choices = possible;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        boolean found = false;
        for (NPC npc : choices) {
            if (input.intValue() == npc.getId()) {
                found = true;
                break;
            }
        }
        CommandSender sender = (CommandSender) context.getForWhom();
        if (!found) {
            Messaging.sendErrorTr(sender, Messages.SELECTION_PROMPT_INVALID_CHOICE, input);
            return this;
        }
        NPC toSelect = CitizensAPI.getNPCRegistry().getById(input.intValue());
        try {
            callback.run(toSelect);
        } catch (ServerCommandException ex) {
            Messaging.sendTr(sender, CommandMessages.MUST_BE_INGAME);
        } catch (CommandUsageException ex) {
            Messaging.sendError(sender, ex.getMessage());
            Messaging.sendError(sender, ex.getUsage());
        } catch (UnhandledCommandException ex) {
            ex.printStackTrace();
        } catch (WrappedCommandException ex) {
            ex.getCause().printStackTrace();
        } catch (CommandException ex) {
            Messaging.sendError(sender, Messaging.tryTranslate(ex.getMessage()));
        } catch (NumberFormatException ex) {
            Messaging.sendErrorTr(sender, CommandMessages.INVALID_NUMBER);
        }
        return null;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        String text = Messaging.tr(Messages.SELECTION_PROMPT);
        for (NPC npc : choices) {
            text += "<br>    - " + npc.getId();
        }
        Messaging.send((CommandSender) context.getForWhom(), text);
        return "";
    }

    public static interface Callback {
        public void run(NPC npc) throws CommandException;
    }

    public static void start(Callback callback, Conversable player, List<NPC> possible) {
        Conversation conversation = new ConversationFactory(CitizensAPI.getPlugin()).withLocalEcho(false)
                .withEscapeSequence("exit").withModality(false)
                .withFirstPrompt(new NPCCommandSelector(callback, possible)).buildConversation(player);
        conversation.begin();
    }

    public static void startWithCallback(Callback callback, NPCRegistry npcRegistry, CommandSender sender,
            CommandContext args, String raw) throws CommandException {
        try {
            UUID uuid = UUID.fromString(raw);
            callback.run(npcRegistry.getByUniqueIdGlobal(uuid));
            return;
        } catch (IllegalArgumentException e) {
        }
        Integer id = Ints.tryParse(raw);
        if (id != null) {
            callback.run(npcRegistry.getById(id));
            return;
        }
        String name = args.getString(1);
        List<NPC> possible = Lists.newArrayList();
        double range = -1;
        if (args.hasValueFlag("range")) {
            range = Math.abs(args.getFlagDouble("range"));
        }
        for (NPC test : npcRegistry) {
            if (test.getName().equalsIgnoreCase(name)) {
                if (range > 0 && test.isSpawned()
                        && !Util.locationWithinRange(args.getSenderLocation(), test.getEntity().getLocation(), range)) {
                    continue;
                }
                possible.add(test);
            }
        }
        if (possible.size() == 1) {
            callback.run(possible.get(0));
        } else if (possible.size() > 1) {
            NPCCommandSelector.start(callback, (Conversable) sender, possible);
        }
    }
}
