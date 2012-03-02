package net.citizensnpcs.trait.text.prompt;

import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.util.Messaging;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

public class TextRemovePrompt extends NumericPrompt {
    private Text text;

    public TextRemovePrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptValidatedInput(ConversationContext context, Number input) {
        int index = input.intValue();
        text.remove(index);
        Messaging.send((Player) context.getForWhom(), "<e>Removed <a>entry at index <e>" + index + "<a>.");
        return new StartPrompt(text);
    }

    @Override
    public String getFailedValidationText(ConversationContext context, String input) {
        return ChatColor.RED + "'" + input + "' is not a valid index!";
    }

    @Override
    public String getPromptText(ConversationContext context) {
        Player player = (Player) context.getForWhom();
        text.sendPage(player, 1);
        return ChatColor.GREEN + "Enter the index of the entry you wish to remove.";
    }

    @Override
    public boolean isNumberValid(ConversationContext context, Number input) {
        return text.hasIndex(input.intValue());
    }
}