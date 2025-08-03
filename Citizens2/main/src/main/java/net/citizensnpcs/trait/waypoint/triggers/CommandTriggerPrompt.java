package net.citizensnpcs.trait.waypoint.triggers;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

public class CommandTriggerPrompt extends StringPrompt implements WaypointTriggerPrompt {
    private final List<String> commands = Lists.newArrayList();

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("back"))
            return (Prompt) context.getSessionData("previous");
        if (input.equalsIgnoreCase("finish")) {
            context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, new CommandTrigger(commands));
            return (Prompt) context.getSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY);
        }
        commands.add(input);
        Messaging.sendTr((CommandSender) context.getForWhom(), Messages.COMMAND_TRIGGER_ADDED, input);
        return this;
    }

    @Override
    public WaypointTrigger createFromShortInput(ConversationContext context, String input) {
        return new CommandTrigger(Lists.newArrayList(input));
    }

    @Override
    public String getPromptText(ConversationContext context) {
        Messaging.sendTr((CommandSender) context.getForWhom(), Messages.COMMAND_TRIGGER_PROMPT);
        return "";
    }
}
