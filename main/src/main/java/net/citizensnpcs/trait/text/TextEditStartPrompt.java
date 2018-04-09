package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class TextEditStartPrompt extends StringPrompt {
    private final Text text;

    public TextEditStartPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();
        try {
            int index = Integer.parseInt(input);
            if (!text.hasIndex(index)) {
                Messaging.sendErrorTr(player, Messages.TEXT_EDITOR_INVALID_INDEX, index);
                return new TextStartPrompt(text);
            }
            context.setSessionData("index", index);
            return new TextEditPrompt(text);
        } catch (NumberFormatException ex) {
            if (input.equalsIgnoreCase("page")) {
                context.setSessionData("previous", this);
                return new PageChangePrompt(text);
            }
        }
        Messaging.sendErrorTr(player, Messages.TEXT_EDITOR_INVALID_INPUT);
        return new TextStartPrompt(text);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        text.sendPage(((Player) context.getForWhom()), 1);
        return Messaging.tr(Messages.TEXT_EDITOR_EDIT_BEGIN_PROMPT);
    }
}