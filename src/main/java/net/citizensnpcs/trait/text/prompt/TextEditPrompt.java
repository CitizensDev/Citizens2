package net.citizensnpcs.trait.text.prompt;

import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class TextEditPrompt extends StringPrompt {
    private Text text;

    public TextEditPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        text.edit((Integer) context.getSessionData("index"), input);
        Messaging.send((Player) context.getForWhom(), "<a>Changed entry at index <e>" + context.getSessionData("index")
                + " <a>to <e>" + input + "<a>.");
        return new StartPrompt(text);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return StringHelper.parseColors("<a>Enter text to change the entry at the index <e>"
                + context.getSessionData("index") + "<a>.");
    }
}