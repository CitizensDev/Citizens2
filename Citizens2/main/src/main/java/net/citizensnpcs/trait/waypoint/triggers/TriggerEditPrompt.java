package net.citizensnpcs.trait.waypoint.triggers;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.google.common.primitives.Ints;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.waypoint.Waypoint;
import net.citizensnpcs.trait.waypoint.WaypointEditor;
import net.citizensnpcs.util.Messages;

public class TriggerEditPrompt extends StringPrompt {
    private final WaypointEditor editor;

    public TriggerEditPrompt(WaypointEditor editor) {
        this.editor = editor;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        input = input.toLowerCase().trim();
        if (input.startsWith("remove_trigger")) {
            Waypoint waypoint = editor.getCurrentWaypoint();
            List<WaypointTrigger> triggers = waypoint.getTriggers();
            int idx = Ints.tryParse(input.replaceFirst("remove_trigger\\s*", ""));
            if (idx < triggers.size()) {
                triggers.remove(idx);
            }
            return this;
        }
        if (input.contains("add")) {
            context.setSessionData("said", false);
            return new TriggerAddPrompt();
        }
        return this;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        WaypointTrigger returned = (WaypointTrigger) context.getSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY);

        if (returned != null) {
            if (editor.getCurrentWaypoint() != null) {
                editor.getCurrentWaypoint().addTrigger(returned);
                Messaging.sendTr((CommandSender) context.getForWhom(), Messages.WAYPOINT_TRIGGER_ADDED_SUCCESSFULLY,
                        returned.description());
            } else {
                Messaging.sendErrorTr((CommandSender) context.getForWhom(), Messages.WAYPOINT_TRIGGER_EDITOR_INACTIVE);
            }
            context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, null);
        }
        context.setSessionData("said", false);
        context.setSessionData("previous", this);
        Messaging.sendTr((CommandSender) context.getForWhom(), Messages.WAYPOINT_TRIGGER_EDITOR_PROMPT);
        if (editor.getCurrentWaypoint() != null) {
            editor.getCurrentWaypoint().describeTriggers((CommandSender) context.getForWhom());
        }
        return "";
    }

    public static Conversation start(Player player, WaypointEditor editor) {
        Conversation conversation = new ConversationFactory(CitizensAPI.getPlugin()).withLocalEcho(false)
                .addConversationAbandonedListener(event -> Messaging
                        .sendTr((CommandSender) event.getContext().getForWhom(), Messages.WAYPOINT_TRIGGER_EDITOR_EXIT))
                .withEscapeSequence("exit").withEscapeSequence("triggers").withEscapeSequence("/npc path")
                .withModality(false).withFirstPrompt(new TriggerEditPrompt(editor)).buildConversation(player);
        conversation.begin();
        return conversation;
    }
}
