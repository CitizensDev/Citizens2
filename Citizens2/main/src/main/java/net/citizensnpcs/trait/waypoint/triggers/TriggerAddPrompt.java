package net.citizensnpcs.trait.waypoint.triggers;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import com.google.common.base.Joiner;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

public class TriggerAddPrompt extends StringPrompt {
    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        input = input.toLowerCase().trim();
        if (input.equalsIgnoreCase("back")) {
            context.setSessionData("said", false);
            return (Prompt) context.getSessionData("previous");
        }
        String[] split = input.split(" ");
        input = split[0];
        split[0] = null;
        Prompt prompt = WaypointTriggerRegistry.getTriggerPromptFrom(input);
        String extraInput = Joiner.on(' ').skipNulls().join(split);
        context.setSessionData("said", false);
        if (prompt == null) {
            Messaging.sendErrorTr((CommandSender) context.getForWhom(),
                    Messages.WAYPOINT_TRIGGER_EDITOR_INVALID_TRIGGER, input);
            return this;
        } else if (extraInput.length() > 0) {
            WaypointTrigger returned = ((WaypointTriggerPrompt) prompt).createFromShortInput(context, extraInput);
            if (returned != null) {
                context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, returned);
                context.setSessionData("said", false);
                return (Prompt) context.getSessionData("previous");
            }
        }
        return prompt;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        if (context.getSessionData("said") == Boolean.TRUE)
            return "";
        context.setSessionData("said", true);
        context.setSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY, context.getSessionData("previous"));
        Messaging.sendTr((CommandSender) context.getForWhom(), Messages.WAYPOINT_TRIGGER_ADD_PROMPT,
                WaypointTriggerRegistry.describeValidTriggerNames());
        return "";
    }
}
