package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class TextEditPrompt extends StringPrompt {
    private final Text text;

    public TextEditPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        int index = (Integer) context.getSessionData("index");
        text.edit(index, input);
        Messaging.sendTr((CommandSender) context.getForWhom(), Messages.TEXT_EDITOR_EDITED_TEXT, index, input);
        return new TextStartPrompt(text);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return ChatColor.GREEN + Messaging.tr(Messages.TEXT_EDITOR_EDIT_PROMPT);
    }
}