package net.citizensnpcs.trait.waypoint.triggers;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

public class DelayTriggerPrompt extends StringPrompt implements WaypointTriggerPrompt {
    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        int delay = Math.max(0, Util.parseTicks(input));
        context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, new DelayTrigger(delay));
        return (Prompt) context.getSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY);
    }

    @Override
    public WaypointTrigger createFromShortInput(ConversationContext context, String input) {
        return new DelayTrigger(Math.max(0, Util.parseTicks(input)));
    }

    @Override
    public String getPromptText(ConversationContext context) {
        Messaging.sendTr((CommandSender) context.getForWhom(), Messages.DELAY_TRIGGER_PROMPT);
        return "";
    }
}
