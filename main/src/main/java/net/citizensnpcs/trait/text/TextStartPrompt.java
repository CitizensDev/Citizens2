package net.citizensnpcs.trait.text;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class TextStartPrompt extends StringPrompt {
    private final Text text;

    public TextStartPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String original) {
        String[] parts = ChatColor.stripColor(original.trim()).split(" ");
        String input = parts[0];
        CommandSender sender = (CommandSender) context.getForWhom();
        if (input.equalsIgnoreCase("add"))
            return new TextAddPrompt(text);
        else if (input.equalsIgnoreCase("edit"))
            return new TextEditStartPrompt(text);
        else if (input.equalsIgnoreCase("remove"))
            return new TextRemovePrompt(text);
        else if (input.equalsIgnoreCase("random"))
            Messaging.sendTr(sender, Messages.TEXT_EDITOR_RANDOM_TALKER_SET, text.toggleRandomTalker());
        else if (input.equalsIgnoreCase("realistic looking"))
            Messaging.sendTr(sender, Messages.TEXT_EDITOR_REALISTIC_LOOKING_SET, text.toggleRealisticLooking());
        else if (input.equalsIgnoreCase("close") || input.equalsIgnoreCase("talk-close"))
            Messaging.sendTr(sender, Messages.TEXT_EDITOR_CLOSE_TALKER_SET, text.toggle());
        else if (input.equalsIgnoreCase("range")) {
            try {
                double range = Math.min(Math.max(0, Double.parseDouble(parts[1])), Setting.MAX_TEXT_RANGE.asDouble());
                text.setRange(range);
                Messaging.sendTr(sender, Messages.TEXT_EDITOR_RANGE_SET, range);
            } catch (NumberFormatException e) {
                Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_RANGE);
            } catch (ArrayIndexOutOfBoundsException e) {
                Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_RANGE);
            }
        } else if (input.equalsIgnoreCase("item")) {
            if (parts.length > 1) {
                text.setItemInHandPattern(parts[1]);
                Messaging.sendTr(sender, Messages.TEXT_EDITOR_SET_ITEM, parts[1]);
            }
        } else if (input.equalsIgnoreCase("help")) {
            context.setSessionData("said-text", false);
            Messaging.send(sender, getPromptText(context));
        } else
            Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_EDIT_TYPE);

        return new TextStartPrompt(text);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        if (context.getSessionData("said-text") == Boolean.TRUE)
            return "";
        String text = Messaging.tr(Messages.TEXT_EDITOR_START_PROMPT);
        context.setSessionData("said-text", Boolean.TRUE);
        return text;
    }
}