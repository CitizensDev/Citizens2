package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.util.Messaging;

public class TextEditPrompt extends StringPrompt {
    private Text text;

    public TextEditPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        int index = (Integer) context.getSessionData("index");
        text.edit(index, input);
        Messaging.send((Player) context.getForWhom(), "<a>Changed entry at index <e>" + index + " <a>to <e>" + input
                + "<a>.");
        return new StartPrompt(text);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return ChatColor.GREEN + "Enter text to edit the entry.";
    }
}