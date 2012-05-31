package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

public class StartPrompt extends StringPrompt {
    private final Text text;

    public StartPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("add"))
            return new TextAddPrompt(text);
        else if (input.equalsIgnoreCase("edit"))
            return new TextEditStartPrompt(text);
        else if (input.equalsIgnoreCase("remove"))
            return new TextRemovePrompt(text);
        else if (input.equalsIgnoreCase("random"))
            Messaging.send((Player) context.getForWhom(), "<e>Random talker <a>set to <e>" + text.toggleRandomTalker()
                    + "<a>.");
        else if (input.equalsIgnoreCase("close"))
            Messaging.send((Player) context.getForWhom(), "<e>Close talker <a>set to <e>" + text.toggle() + "<a>.");
        else
            Messaging.sendError((Player) context.getForWhom(), "Invalid edit type.");

        return new StartPrompt(text);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return StringHelper
                .parseColors("<a>Type <e>add <a>to add an entry, <e>edit <a>to edit entries, <e>remove <a>to remove entries, <e>close <a>to toggle the NPC as a close talker, and <e>random <a>to toggle the NPC as a random talker.");
    }
}