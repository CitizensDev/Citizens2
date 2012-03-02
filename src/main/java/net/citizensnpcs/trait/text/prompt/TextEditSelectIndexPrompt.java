package net.citizensnpcs.trait.text.prompt;

import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

public class TextEditSelectIndexPrompt extends NumericPrompt {
    private Text text;

    public TextEditSelectIndexPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptValidatedInput(ConversationContext context, Number input) {
        context.setSessionData("index", input.intValue());
        Messaging.send((Player) context.getForWhom(), "<a>Now <e>editing <a>the entry at index <e>" + input.intValue()
                + "<a>.");
        return new TextEditPrompt(text);
    }

    @Override
    public String getFailedValidationText(ConversationContext context, String input) {
        return ChatColor.RED + "'" + input + "' is not a valid index!";
    }

    @Override
    public String getPromptText(ConversationContext context) {
        Player player = (Player) context.getForWhom();
        text.sendPage(player, 1);
        return StringHelper.parseColors("<a>Enter the index of the entry you wish to edit.");
    }

    @Override
    public boolean isNumberValid(ConversationContext context, Number input) {
        return text.hasIndex(input.intValue());
    }
}