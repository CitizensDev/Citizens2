package net.citizensnpcs.trait.text;

import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

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
        if (input.equalsIgnoreCase("add"))
            return new TextAddPrompt(text);
        else if (input.equalsIgnoreCase("edit"))
            return new TextEditStartPrompt(text);
        else if (input.equalsIgnoreCase("remove"))
            return new TextRemovePrompt(text);
        else if (input.equalsIgnoreCase("random"))
            Messaging.send((CommandSender) context.getForWhom(),
                    "<e>Random talker <a>set to <e>" + text.toggleRandomTalker() + "<a>.");
        else if (input.equalsIgnoreCase("realistic looking"))
            Messaging.send((CommandSender) context.getForWhom(),
                    "<e>Realistic looking <a>set to <e>" + text.toggleRealisticLooking() + "<a>.");
        else if (input.equalsIgnoreCase("close"))
            Messaging.send((CommandSender) context.getForWhom(),
                    "<e>Close talker <a>set to <e>" + text.toggle() + "<a>.");
        else if (input.equalsIgnoreCase("help")) {
            context.setSessionData("said-text", false);
            Messaging.send((CommandSender) context.getForWhom(), getPromptText(context));
        } else
            Messaging.sendError((CommandSender) context.getForWhom(), "Invalid edit type.");

        return new StartPrompt(text);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        if (context.getSessionData("said-text") == Boolean.TRUE)
            return "";
        String text = StringHelper
                .parseColors("<a>Type <e>add <a>to add an entry, <e>edit <a>to edit entries, <e>remove <a>to remove entries, <e>close <a>to toggle the NPC as a close talker, and <e>random <a>to toggle the NPC as a random talker. Type <e>help<a> to show this again.");
        context.setSessionData("said-text", Boolean.TRUE);
        return text;
    }
}