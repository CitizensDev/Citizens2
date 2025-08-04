package net.citizensnpcs.trait.waypoint.triggers;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

public class SpeedTriggerPrompt extends NumericPrompt implements WaypointTriggerPrompt {
    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        float speed = (float) Math.max(input.doubleValue(), 0);
        context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, new SpeedTrigger(speed));
        return (Prompt) context.getSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY);
    }

    @Override
    public WaypointTrigger createFromShortInput(ConversationContext context, String input) {
        try {
            float speed = (float) Math.max(Double.parseDouble(input), 0);
            return new SpeedTrigger(speed);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public String getPromptText(ConversationContext context) {
        Messaging.sendTr((CommandSender) context.getForWhom(), Messages.SPEED_TRIGGER_PROMPT);
        return "";
    }
}
