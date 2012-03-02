package net.citizensnpcs.trait.text.prompt;

import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class TextAddPrompt extends StringPrompt {
    private Text text;

    public TextAddPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String string) {
        text.add(string);
        context.getForWhom().sendRawMessage(StringHelper.parseColors("<e>Added <a>the entry <e>" + string + "."));
        return new StartPrompt(text);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return "Enter text to add to the NPC.";
    }
}