package net.citizensnpcs.trait.waypoint.triggers;

import java.util.List;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import com.google.common.collect.Lists;

public class ChatTriggerPrompt extends StringPrompt implements WaypointTriggerPrompt {
    private final List<String> lines = Lists.newArrayList();
    private double radius = -1;

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("back"))
            return (Prompt) context.getSessionData("previous");
        if (input.startsWith("radius")) {
            try {
                radius = Double.parseDouble(input.split(" ")[1]);
            } catch (NumberFormatException e) {
                Messaging.sendErrorTr((CommandSender) context.getForWhom(),
                        Messages.WAYPOINT_TRIGGER_CHAT_INVALID_RADIUS);
            } catch (IndexOutOfBoundsException e) {
                Messaging.sendErrorTr((CommandSender) context.getForWhom(), Messages.WAYPOINT_TRIGGER_CHAT_NO_RADIUS);
            }
            return this;
        }
        if (input.equalsIgnoreCase("finish")) {
            context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, new ChatTrigger(radius, lines));
            return (Prompt) context.getSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY);
        }
        lines.add(input);
        return this;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        Messaging.sendTr((CommandSender) context.getForWhom(), Messages.CHAT_TRIGGER_PROMPT);
        return "";
    }
}
