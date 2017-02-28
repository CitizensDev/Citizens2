package net.citizensnpcs.trait.waypoint.triggers;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.waypoint.WaypointEditor;
import net.citizensnpcs.util.Messages;

public class TriggerRemovePrompt extends StringPrompt {
    private final WaypointEditor editor;

    public TriggerRemovePrompt(WaypointEditor editor) {
        this.editor = editor;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("back")) {
            context.setSessionData("said", false);
            return (Prompt) context.getSessionData("previous");
        }
        if (editor.getCurrentWaypoint() == null) {
            Messaging.sendErrorTr((CommandSender) context.getForWhom(), Messages.WAYPOINT_TRIGGER_EDITOR_INACTIVE);
            return this;
        }
        int index = 0;
        try {
            index = Math.max(0, Integer.parseInt(input) - 1);
        } catch (NumberFormatException e) {
            Messaging.sendErrorTr((CommandSender) context.getForWhom(),
                    Messages.WAYPOINT_TRIGGER_REMOVE_INVALID_NUMBER);
            return this;
        }
        List<WaypointTrigger> triggers = editor.getCurrentWaypoint().getTriggers();
        if (index >= triggers.size()) {
            Messaging.sendErrorTr((CommandSender) context.getForWhom(),
                    Messages.WAYPOINT_TRIGGER_REMOVE_INDEX_OUT_OF_RANGE, triggers.size());
        } else {
            triggers.remove(index);
            Messaging.sendTr((CommandSender) context.getForWhom(), Messages.WAYPOINT_TRIGGER_REMOVE_REMOVED, index + 1);
        }
        return this;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        if (editor.getCurrentWaypoint() == null) {
            Messaging.sendErrorTr((CommandSender) context.getForWhom(), Messages.WAYPOINT_TRIGGER_EDITOR_INACTIVE);
            return "";
        }
        if (context.getSessionData("said") == Boolean.TRUE)
            return "";
        context.setSessionData("said", true);
        String root = Messaging.tr(Messages.WAYPOINT_TRIGGER_REMOVE_PROMPT);
        int i = 1;
        for (WaypointTrigger trigger : editor.getCurrentWaypoint().getTriggers()) {
            root += String.format("<br>     %d. " + trigger.description(), i++);
        }
        Messaging.send((CommandSender) context.getForWhom(), root);
        return "";
    }
}
