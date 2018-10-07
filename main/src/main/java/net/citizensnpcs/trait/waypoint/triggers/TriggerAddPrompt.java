package net.citizensnpcs.trait.waypoint.triggers;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import com.google.common.base.Joiner;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.waypoint.WaypointEditor;
import net.citizensnpcs.util.Messages;

public class TriggerAddPrompt extends StringPrompt {
    private final WaypointEditor editor;

    public TriggerAddPrompt(WaypointEditor editor) {
        this.editor = editor;
    }

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
        if (prompt == null) {
            Messaging.sendErrorTr((CommandSender) context.getForWhom(),
                    Messages.WAYPOINT_TRIGGER_EDITOR_INVALID_TRIGGER, input);
            context.setSessionData("said", false);
            return this;
        } else if (extraInput.length() > 0) {
            WaypointTrigger returned = ((WaypointTriggerPrompt) prompt).createFromShortInput(context, extraInput);
            if (returned != null) {
                context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, returned);
                return this;
            }
        }
        return prompt;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        WaypointTrigger returned = (WaypointTrigger) context.getSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY);

        if (returned != null) {
            if (editor.getCurrentWaypoint() != null) {
                editor.getCurrentWaypoint().addTrigger(returned);
                context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, null);
                Messaging.sendTr((CommandSender) context.getForWhom(), Messages.WAYPOINT_TRIGGER_ADDED_SUCCESSFULLY,
                        returned.description());
                String base = "";
                for (WaypointTrigger trigger : editor.getCurrentWaypoint().getTriggers()) {
                    base += "\n    - " + trigger.description();
                }
                Messaging.sendTr((CommandSender) context.getForWhom(), Messages.WAYPOINT_TRIGGER_LIST, base);
            } else {
                Messaging.sendErrorTr((CommandSender) context.getForWhom(), Messages.WAYPOINT_TRIGGER_EDITOR_INACTIVE);
            }
        }
        if (context.getSessionData("said") == Boolean.TRUE)
            return "";
        context.setSessionData("said", true);
        context.setSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY, this);
        return Messaging.tr(Messages.WAYPOINT_TRIGGER_ADD_PROMPT, WaypointTriggerRegistry.describeValidTriggerNames());
    }
}
