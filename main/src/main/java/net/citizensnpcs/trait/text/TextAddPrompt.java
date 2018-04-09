package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class TextAddPrompt extends StringPrompt {
    private final Text text;

    public TextAddPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        text.add(input);
        Messaging.sendTr((Player) context.getForWhom(), Messages.TEXT_EDITOR_ADDED_ENTRY, input);
        return new TextStartPrompt(text);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return ChatColor.GREEN + Messaging.tr(Messages.TEXT_EDITOR_ADD_PROMPT);
    }
}