package net.citizensnpcs.trait.waypoint.triggers;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.Util;

public class AnimationTriggerPrompt extends StringPrompt implements WaypointTriggerPrompt {
    private final List<PlayerAnimation> animations = Lists.newArrayList();
    private Location at;

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("back"))
            return (Prompt) context.getSessionData("previous");
        if (input.startsWith("at ")) {
            try {
                at = CommandContext.parseLocation(
                        context.getForWhom() instanceof Player ? ((Player) context.getForWhom()).getLocation() : null,
                        input.replaceFirst("at ", ""));
                Messaging.send((CommandSender) context.getForWhom(), Messages.WAYPOINT_TRIGGER_ANIMATION_AT_SET,
                        Util.prettyPrintLocation(at));
            } catch (CommandException e) {
                Messaging.send((CommandSender) context.getForWhom(), e.getMessage());
            }
            return this;
        }
        if (input.equalsIgnoreCase("finish")) {
            context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, new AnimationTrigger(animations, at));
            return (Prompt) context.getSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY);
        }
        PlayerAnimation animation = Util.matchEnum(PlayerAnimation.values(), input);
        if (animation == null) {
            Messaging.sendErrorTr((CommandSender) context.getForWhom(), Messages.INVALID_ANIMATION, input,
                    getValidAnimations());
        }
        animations.add(animation);
        Messaging.sendTr((CommandSender) context.getForWhom(), Messages.ANIMATION_ADDED, input);
        return this;
    }

    @Override
    public WaypointTrigger createFromShortInput(ConversationContext context, String input) {
        PlayerAnimation anim = Util.matchEnum(PlayerAnimation.values(), input);
        if (anim == null)
            return null;

        return new AnimationTrigger(Lists.newArrayList(anim), at);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        Messaging.sendTr((CommandSender) context.getForWhom(), Messages.ANIMATION_TRIGGER_PROMPT, getValidAnimations());
        return "";
    }

    private String getValidAnimations() {
        return Joiner.on(", ").join(PlayerAnimation.values());
    }
}
