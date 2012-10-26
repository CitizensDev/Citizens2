package net.citizensnpcs.trait.waypoint.triggers;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.trait.waypoint.Waypoint;
import net.citizensnpcs.trait.waypoint.WaypointEditor;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class TriggerEditPrompt extends StringPrompt {
    private final WaypointEditor editor;

    public TriggerEditPrompt(WaypointEditor editor) {
        this.editor = editor;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        input = input.toLowerCase().trim();
        if (input.contains("add"))
            return new TriggerAddPrompt(editor);
        if (input.contains("remove"))
            return new TriggerRemovePrompt(editor);
        return this;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        context.setSessionData("previous", this);
        context.setSessionData("exit", false);
        if (editor.getCurrentWaypoint() == null)
            return Messaging.tr(Messages.WAYPOINT_TRIGGER_EDITOR_INACTIVE);

        String base = Messaging.tr(Messages.WAYPOINT_TRIGGER_EDITOR_PROMPT);
        Waypoint waypoint = editor.getCurrentWaypoint();
        for (WaypointTrigger trigger : waypoint.getTriggers()) {
            base += "\n    - " + trigger.description();
        }
        Messaging.send((CommandSender) context.getForWhom(), base);
        return "";
    }

    public static void start(Player player, WaypointEditor editor) {
        final Conversation conversation = new ConversationFactory(CitizensAPI.getPlugin())
                .withLocalEcho(false).withEscapeSequence("exit").withModality(false)
                .withFirstPrompt(new TriggerEditPrompt(editor)).buildConversation(player);
        conversation.begin();
    }
}
