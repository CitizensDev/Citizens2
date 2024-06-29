package net.citizensnpcs.trait.waypoint.triggers;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

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
                Messaging.sendTr((CommandSender) context.getForWhom(), Messages.CHAT_TRIGGER_RADIUS_SET, radius);
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
        Messaging.sendTr((CommandSender) context.getForWhom(), Messages.CHAT_TRIGGER_MESSAGE_ADDED, input);
        return this;
    }

    @Override
    public WaypointTrigger createFromShortInput(ConversationContext context, String input) {
        return new ChatTrigger(radius, Lists.newArrayList(input));
    }

    @Override
    public String getPromptText(ConversationContext context) {
        if (context.getSessionData("said") == Boolean.TRUE) {
            Messaging.send((CommandSender) context.getForWhom(),
                    "Current lines:<br>-   " + Joiner.on("<br>-   ").join(lines));
        } else {
            Messaging.sendTr((CommandSender) context.getForWhom(), Messages.CHAT_TRIGGER_PROMPT);
            context.setSessionData("said", true);
        }
        return "";
    }
}
