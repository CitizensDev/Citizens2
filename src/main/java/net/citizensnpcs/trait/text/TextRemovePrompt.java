package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

public class TextRemovePrompt extends StringPrompt {
    private final Text text;

    public TextRemovePrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        Player player = (Player) context.getForWhom();
        try {
            int index = Integer.parseInt(input);
            if (!text.hasIndex(index)) {
                Messaging.sendErrorF(player, "'%d' is not a valid index!", index);
                return new StartPrompt(text);
            }
            text.remove(index);
            Messaging.sendF(player, "<e>Removed <a>entry at index <e>%d<a>.", index);
            return new StartPrompt(text);
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
                .parseColors("<a>Enter the index of the entry you wish to remove or <e>page <a>to view more pages.");
    }
}