package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

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