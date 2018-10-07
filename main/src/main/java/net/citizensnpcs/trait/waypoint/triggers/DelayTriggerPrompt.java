package net.citizensnpcs.trait.waypoint.triggers;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

public class DelayTriggerPrompt extends NumericPrompt implements WaypointTriggerPrompt {
    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        int delay = Math.max(input.intValue(), 0);
        context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, new DelayTrigger(delay));
        return (Prompt) context.getSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY);
    }

    @Override
    public WaypointTrigger createFromShortInput(ConversationContext context, String input) {
        try {
            int delay = Math.max(Integer.parseInt(input), 0);
            return new DelayTrigger(delay);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return Messaging.tr(Messages.DELAY_TRIGGER_PROMPT);
    }
}
