package net.citizensnpcs.npc.ai.speech;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.VocalChord;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class Chat implements VocalChord {
    public final String VOCAL_CHORD_NAME = "chat";

    @Override
    public String getName() {
        return VOCAL_CHORD_NAME;
    }

    @Override
    public void talk(SpeechContext context) {
        if (context.getTalker() == null)
            return;
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(context.getTalker().getEntity());
        if (npc == null)
            return;

        // chat to the world with CHAT_FORMAT and CHAT_RANGE settings
        if (!context.hasRecipients()) {
            String text = Setting.CHAT_FORMAT.asString().replace("<npc>", npc.getName())
                    .replace("<text>", context.getMessage());
            talkToBystanders(npc, text, context);
            return;
        }

        // Assumed recipients at this point
        else if (context.size() <= 1) {
            String text = Setting.CHAT_FORMAT_TO_TARGET.asString().replace("<npc>", npc.getName())
                    .replace("<text>", context.getMessage());
            String targetName = "";
            // For each recipient
            for (Talkable entity : context) {
                entity.talkTo(context, text, this);
                targetName = entity.getName();
            }
            // Check if bystanders hear targeted chat
            if (!Setting.CHAT_BYSTANDERS_HEAR_TARGETED_CHAT.asBoolean())
                return;
            // Format message with config setting and send to bystanders
            String bystanderText = Setting.CHAT_FORMAT_TO_BYSTANDERS.asString().replace("<npc>", npc.getName())
                    .replace("<target>", targetName).replace("<text>", context.getMessage());
            talkToBystanders(npc, bystanderText, context);
            return;
        }

        else { // Multiple recipients
            String text = Setting.CHAT_FORMAT_TO_TARGET.asString().replace("<npc>", npc.getName())
                    .replace("<text>", context.getMessage());
            List<String> targetNames = new ArrayList<String>();
            // Talk to each recipient
            for (Talkable entity : context) {
                entity.talkTo(context, text, this);
                targetNames.add(entity.getName());
            }

            if (!Setting.CHAT_BYSTANDERS_HEAR_TARGETED_CHAT.asBoolean())
                return;
            String targets = "";
            int max = Setting.CHAT_MAX_NUMBER_OF_TARGETS.asInt();
            String[] format = Setting.CHAT_MULTIPLE_TARGETS_FORMAT.asString().split("\\|");
            if (format.length != 4)
                Messaging.severe("npc.chat.options.multiple-targets-format invalid!");
            if (max == 1) {
                targets = format[0].replace("<target>", targetNames.get(0)) + format[3];
            } else if (max == 2 || targetNames.size() == 2) {
                if (targetNames.size() == 2) {
                    targets = format[0].replace("<target>", targetNames.get(0))
                            + format[2].replace("<target>", targetNames.get(1));
                } else
                    targets = format[0].replace("<target>", targetNames.get(0))
                            + format[1].replace("<target>", targetNames.get(1)) + format[3];
            } else if (max >= 3) {
                targets = format[0].replace("<target>", targetNames.get(0));

                int x = 1;
                for (x = 1; x < max - 1; x++) {
                    if (targetNames.size() - 1 == x)
                        break;
                    targets = targets + format[1].replace("<npc>", targetNames.get(x));
                }
                if (targetNames.size() == max) {
                    targets = targets + format[2].replace("<npc>", targetNames.get(x));
                } else
                    targets = targets + format[3];
            }

            String bystanderText = Setting.CHAT_FORMAT_WITH_TARGETS_TO_BYSTANDERS.asString()
                    .replace("<npc>", npc.getName()).replace("<targets>", targets)
                    .replace("<text>", context.getMessage());
            talkToBystanders(npc, bystanderText, context);
        }
    }

    private void talkToBystanders(NPC npc, String text, SpeechContext context) {
        // Get list of nearby entities
        List<Entity> bystanderEntities = npc.getBukkitEntity().getNearbyEntities(Setting.CHAT_RANGE.asDouble(),
                Setting.CHAT_RANGE.asDouble(), Setting.CHAT_RANGE.asDouble());
        for (Entity bystander : bystanderEntities)
            // Continue if a LivingEntity, which is compatible with
            // TalkableEntity
            if (bystander instanceof LivingEntity) {

                boolean should_talk = true;
                // Exclude targeted recipients
                if (context.hasRecipients()) {
                    for (Talkable target : context)
                        if (target.getEntity().equals(bystander))
                            should_talk = false;
                }

                // Found a nearby LivingEntity, make it Talkable and
                // talkNear it if 'should_talk'
                if (should_talk)
                    new TalkableEntity((LivingEntity) bystander).talkNear(context, text, this);

            }
    }

}
