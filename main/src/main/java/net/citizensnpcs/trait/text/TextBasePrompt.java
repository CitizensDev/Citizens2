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
            return new TextEditStartPrompt(text);
        } else if (input.equalsIgnoreCase("remove")) {
            return new TextRemovePrompt(text);
        } else if (input.equalsIgnoreCase("delay")) {
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
            Messaging.sendTr(sender, Messages.TEXT_EDITOR_RANDOM_TALKER_SET, text.toggleRandomTalker());
        } else if (original.trim().equalsIgnoreCase("realistic looking")) {
            Messaging.sendTr(sender, Messages.TEXT_EDITOR_REALISTIC_LOOKING_SET, text.toggleRealisticLooking());
        } else if (original.trim().equalsIgnoreCase("speech bubbles")) {
            Messaging.sendTr(sender, Messages.TEXT_EDITOR_SPEECH_BUBBLES_SET, text.toggleSpeechBubbles());
        } else if (input.equalsIgnoreCase("close") || original.trim().equalsIgnoreCase("talk close")) {
            Messaging.sendTr(sender, Messages.TEXT_EDITOR_CLOSE_TALKER_SET, text.toggle());
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
        } else if (input.equalsIgnoreCase("help")) {
            context.setSessionData("said-text", false);
            Messaging.send(sender, getPromptText(context));
        } else {
            Messaging.sendErrorTr(sender, Messages.TEXT_EDITOR_INVALID_EDIT_TYPE);
        }

        return this;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        if (Boolean.TRUE == context.getSessionData("said-text")) {
            text.sendPage(((Player) context.getForWhom()), 1);
        } else {
            Messaging.send((Player) context.getForWhom(), Messaging.tr(Messages.TEXT_EDITOR_START_PROMPT));
            text.sendPage(((Player) context.getForWhom()), 1);
            context.setSessionData("said-text", Boolean.TRUE);
        }
        return "";
    }
}