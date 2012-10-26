package net.citizensnpcs.trait.waypoint.triggers;

import net.citizensnpcs.trait.waypoint.WaypointEditor;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

public class TriggerAddPrompt extends StringPrompt {
    private final WaypointEditor editor;

    public TriggerAddPrompt(WaypointEditor editor) {
        this.editor = editor;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        input = input.toLowerCase().trim();
        if (input.equalsIgnoreCase("back"))
            return (Prompt) context.getSessionData("previous");
        Prompt prompt = WaypointTriggerRegistry.getTriggerPromptFrom(input);
        if (prompt == null) {
            Messaging.sendErrorTr((CommandSender) context.getForWhom(),
                    Messages.WAYPOINT_TRIGGER_EDITOR_INVALID_TRIGGER, input);
            return this;
        }
        return prompt;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        WaypointTrigger returned = (WaypointTrigger) context
                .getSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY);
        if (returned != null) {
            if (editor.getCurrentWaypoint() != null) {
                editor.getCurrentWaypoint().addTrigger(returned);
                context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, null);
                Messaging.sendTr((CommandSender) context.getForWhom(),
                        Messages.WAYPOINT_TRIGGER_ADDED_SUCCESSFULLY, returned.description());
            } else
                Messaging.sendErrorTr((CommandSender) context.getForWhom(),
                        Messages.WAYPOINT_TRIGGER_EDITOR_INACTIVE);
        }
        context.setSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY, this);
        return Messaging.tr(Messages.WAYPOINT_TRIGGER_ADD_PROMPT,
                WaypointTriggerRegistry.describeValidTriggerNames());
    }
}
