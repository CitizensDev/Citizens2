package net.citizensnpcs.trait.text;

import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class StartPrompt extends StringPrompt {
    private final Text text;

    public StartPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        CommandSender sender = (CommandSender) context.getForWhom();
        if (input.equalsIgnoreCase("add"))
            return new TextAddPrompt(text);
        else if (input.equalsIgnoreCase("edit"))
            return new TextEditStartPrompt(text);
        else if (input.equalsIgnoreCase("remove"))
            return new TextRemovePrompt(text);
        else if (input.equalsIgnoreCase("random"))
            Messaging.send(sender, "[[Random talker]] set to [[" + text.toggleRandomTalker() + "]].");
        else if (input.equalsIgnoreCase("realistic looking"))
            Messaging.send(sender, "[[Realistic looking]] set to [[" + text.toggleRealisticLooking() + "]].");
        else if (input.equalsIgnoreCase("close"))
            Messaging.send(sender, "[[Close talker]] set to [[" + text.toggle() + "]].");
        else if (input.equalsIgnoreCase("help")) {
            context.setSessionData("said-text", false);
            Messaging.send(sender, getPromptText(context));
        } else
            Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_EDIT_TYPE);

        return new StartPrompt(text);
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