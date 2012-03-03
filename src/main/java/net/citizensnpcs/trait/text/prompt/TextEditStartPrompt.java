package net.citizensnpcs.trait.text.prompt;

import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class TextEditStartPrompt extends StringPrompt {
    private Text text;

    public TextEditStartPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();
        try {
            int index = Integer.parseInt(input);
            if (!text.hasIndex(index)) {
                Messaging.sendError(player, "'" + index + "' is not a valid index!");
                return new StartPrompt(text);
            }
            context.setSessionData("index", index);
            return new TextEditPrompt(text);
        } catch (NumberFormatException ex) {
            if (input.equalsIgnoreCase("page")) {
                context.setSessionData("previous", this);
                return new PageChangePrompt(text);
            }
        }
        Messaging.sendError(player, "Invalid input.");
        return new StartPrompt(text);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        text.sendPage(((Player) context.getForWhom()), 1);
        return StringHelper
                .parseColors("<a>Enter the index of the entry you wish to edit or <e>page <a>to view more pages.");
    }
}