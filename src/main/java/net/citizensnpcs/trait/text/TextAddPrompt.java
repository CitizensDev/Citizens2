package net.citizensnpcs.trait.text;

import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class TextAddPrompt extends StringPrompt {
    private Text text;

    public TextAddPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        text.add(input);
        Messaging.send((Player) context.getForWhom(),
                StringHelper.parseColors("<e>Added <a>the entry <e>" + input + "."));
        return new StartPrompt(text);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return ChatColor.GREEN + "Enter text to add to the NPC.";
    }
}