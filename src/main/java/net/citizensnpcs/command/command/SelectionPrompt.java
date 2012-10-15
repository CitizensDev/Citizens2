package net.citizensnpcs.command.command;

import java.util.List;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

public class SelectionPrompt extends NumericPrompt {
    private final List<NPC> choices;
    private final NPCSelector selector;

    public SelectionPrompt(NPCSelector selector, List<NPC> possible) {
        choices = possible;
        this.selector = selector;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        Object num = context.getSessionData(input);
        if (num == null) {
            for (NPC npc : choices) {
                if (input.intValue() == npc.getId()) {
                    num = input.intValue();
                    break;
                }
            }
            if (num == null)
                return this;
        }
        NPC toSelect = CitizensAPI.getNPCRegistry().getById((Integer) num);
        CommandSender sender = (CommandSender) context.getForWhom();
        selector.select(sender, toSelect);
        Messaging.sendWithNPC(sender, Setting.SELECTION_MESSAGE.asString(), toSelect);
        return null;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        String text = Messaging.tr(Messages.SELECTION_PROMPT);
        int num = 1;
        for (NPC npc : choices) {
            text += "<br>    - " + npc.getId() + "(" + num + ")";
            context.setSessionData(npc.getId(), num);
            num++;
        }
        return text;
    }

    public static void start(NPCSelector selector, Player player, List<NPC> possible) {
        final Conversation conversation = new ConversationFactory(CitizensAPI.getPlugin())
                .withLocalEcho(false).withEscapeSequence("exit").withModality(false)
                .withFirstPrompt(new SelectionPrompt(selector, possible)).buildConversation(player);
        conversation.begin();
    }
}
