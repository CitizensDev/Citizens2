package net.citizensnpcs.trait.text.prompt;

import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class StartPrompt extends StringPrompt {
    private Text text;

    public StartPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String string) {
        if (string.equalsIgnoreCase("add"))
            return new TextAddPrompt(text);
        else if (string.equalsIgnoreCase("edit"))
            return new TextEditSelectIndexPrompt(text);
        else if (string.equalsIgnoreCase("remove"))
            return new TextRemovePrompt(text);
        else {
            Messaging.sendError((Player) context.getForWhom(), "Invalid edit type.");
            return new StartPrompt(text);
        }
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return StringHelper
                .parseColors("<a>Type <e>add <a>to add an entry, <e>edit <a>to edit entries, and <e>remove <a>to remove entries.");
    }
}