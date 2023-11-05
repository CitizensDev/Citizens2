package net.citizensnpcs.trait.text;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import com.google.common.base.Joiner;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

public class TextBasePrompt extends StringPrompt {
    private final Text text;

    public TextBasePrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String original) {
        String[] parts = ChatColor.stripColor(original.trim()).split(" ");
        String input = parts[0];

        CommandSender sender = (CommandSender) context.getForWhom();
        if (input.equalsIgnoreCase("add")) {
            text.add(Joiner.on(' ').join(Arrays.copyOfRange(parts, 1, parts.length)));
            return this;
        } else if (input.equalsIgnoreCase("edit")) {
            if (parts.length < 2) {
                Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_INDEX, "missing index");
            } else {
                int index = Integer.parseInt(parts[1]);
                if (!text.hasIndex(index)) {
                    Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_INDEX, index);
                } else {
                    text.edit(index, Joiner.on(' ').join(Arrays.copyOfRange(parts, 2, parts.length)));
                }
            }
        } else if (input.equalsIgnoreCase("remove")) {
            int index = Integer.parseInt(parts[1]);
            if (!text.hasIndex(index)) {
                Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_INDEX);
            } else {
                text.remove(index);
            }
        } else if (input.equalsIgnoreCase("page")) {
            try {
                int page = Integer.parseInt(parts[1]);
                if (!text.hasPage(page)) {
                    Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_PAGE);
                }
                context.setSessionData("page", page);
            } catch (NumberFormatException e) {
                Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_PAGE);
            }
        }
        Messaging.send(sender, getPromptText(context));

        if (input.equalsIgnoreCase("delay")) {
            try {
                int delay = Integer.parseInt(parts[1]);
                text.setDelay(delay);
                Messaging.sendTr(sender, Messages.TEXT_EDITOR_DELAY_SET, delay);
            } catch (NumberFormatException e) {
                Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_DELAY);
            } catch (ArrayIndexOutOfBoundsException e) {
                Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_DELAY);
            }
        } else if (input.equalsIgnoreCase("random")) {
            text.toggleRandomTalker();
        } else if (original.trim().equalsIgnoreCase("realistic looking")) {
            text.toggleRealisticLooking();
        } else if (original.trim().equalsIgnoreCase("speech bubbles")) {
            text.toggleSpeechBubbles();
        } else if (input.equalsIgnoreCase("close") || original.trim().equalsIgnoreCase("talk close")) {
            text.toggleTalkClose();
        } else if (input.equalsIgnoreCase("range")) {
            try {
                double range = Math.min(Math.max(0, Double.parseDouble(parts[1])), Setting.MAX_TEXT_RANGE.asDouble());
                text.setRange(range);
                Messaging.sendTr(sender, Messages.TEXT_EDITOR_RANGE_SET, range);
            } catch (NumberFormatException e) {
                Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_RANGE);
            } catch (ArrayIndexOutOfBoundsException e) {
                Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_RANGE);
            }
        } else if (input.equalsIgnoreCase("item")) {
            if (parts.length > 1) {
                text.setItemInHandPattern(parts[1]);
                Messaging.sendTr(sender, Messages.TEXT_EDITOR_SET_ITEM, parts[1]);
            } else {
                Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_MISSING_ITEM_PATTERN);
            }
        } else {
            Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_EDIT_TYPE);
        }
        return this;
    }

    private String colorToggleableText(boolean enabled) {
        return (enabled ? "<green>" : "<red>").toString();
    }

    @Override
    public String getPromptText(ConversationContext context) {
        Messaging.send((Player) context.getForWhom(),
                Messaging.tr(Messages.TEXT_EDITOR_START_PROMPT, colorToggleableText(text.shouldTalkClose()),
                        colorToggleableText(text.isRandomTalker()), colorToggleableText(text.useSpeechBubbles()),
                        colorToggleableText(text.useRealisticLooking())));
        int page = context.getSessionData("page") == null ? 1 : (int) context.getSessionData("page");
        text.sendPage((Player) context.getForWhom(), page);
        return "";
    }
}