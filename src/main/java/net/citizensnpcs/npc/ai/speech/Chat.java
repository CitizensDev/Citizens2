package net.citizensnpcs.npc.ai.speech;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.TalkableEntity;
import net.citizensnpcs.api.ai.speech.Tongue;
import net.citizensnpcs.api.ai.speech.VocalChord;
import net.citizensnpcs.api.npc.NPC;

public class Chat implements VocalChord {

	/*
    	CHAT_FORMAT("npc.chat.format.no-targets", "[<npc>]: <text>"),
        CHAT_FORMAT_TO_TARGET("npc.chat.format.to-target", "[<npc>] -> You: <text>"),
        CHAT_FORMAT_TO_BYSTANDERS("npc.chat.prefix.to-bystanders", "[<npc>] -> [<target>]: <text>"),
        CHAT_FORMAT_WITH_TARGETS_TO_BYSTANDERS("npc.chat.format.with-target-to-bystanders", "[<npc>] -> [<targets>]: <text>"),
        CHAT_RANGE("npc.chat.options.range", 5),
        CHAT_BYSTANDERS_HEAR_TARGETED_CHAT("npc.chat.options.bystanders-hear-targeted-chat", true),
        CHAT_MAX_NUMBER_OF_TARGETS("npc.chat.options.max-number-of-targets-to-show", 2),
        CHAT_MULTIPLE_TARGETS_FORMAT("npc.chat.options.multiple-targets-format", "<target>,|<target>|& <target>|& others"),
	 */

	public final String VOCAL_CHORD_NAME = "chat";
	
	@Override
	public void talk(Tongue tongue) {

		NPC npc = CitizensAPI.getNPCRegistry().getNPC(tongue.getTalker().getEntity());

		// If no recipients, chat to the world with CHAT_FORMAT and CHAT_RANGE settings
		if (!tongue.isTargeted()) {
			String text = Setting.CHAT_FORMAT.asString().replace("<npc>", npc.getName()).replace("<text>", tongue.getContents());
			talkToBystanders(npc, text, tongue);
			return;
		}

		// Assumed recipients at this point
		else if (tongue.getRecipients().size() <= 1) { // One recipient
			String text = Setting.CHAT_FORMAT_TO_TARGET.asString().replace("<npc>", npc.getName()).replace("<text>", tongue.getContents());
			tongue.getRecipients().get(0).talkTo(tongue, text, this);
			if (!Setting.CHAT_BYSTANDERS_HEAR_TARGETED_CHAT.asBoolean()) return;
			String bystanderText = Setting.CHAT_FORMAT_TO_BYSTANDERS.asString().replace("<npc>", npc.getName()).replace("<target>", tongue.getRecipients().get(0).getName()).replace("<text>", tongue.getContents());
			talkToBystanders(npc, bystanderText, tongue);
			return;
		}
		
		else { // Multiple recipients
			// Set up text
			String text = Setting.CHAT_FORMAT_TO_TARGET.asString().replace("<npc>", npc.getName()).replace("<text>", tongue.getContents());
			tongue.getRecipients().get(0).talkTo(tongue, text, this);
			if (!Setting.CHAT_BYSTANDERS_HEAR_TARGETED_CHAT.asBoolean()) return;
			String bystanders = null;
			bystanders = bystanders + "";
			String bystanderText = Setting.CHAT_FORMAT_WITH_TARGETS_TO_BYSTANDERS.asString().replace("<npc>", npc.getName()).replace("<targets>", tongue.getRecipients().get(0).getName()).replace("<text>", tongue.getContents());
			talkToBystanders(npc, bystanderText, tongue);

			// TODO: Finish multiple recipients
		
		}
	}

	private void talkToBystanders(NPC npc, String text, Tongue tongue) {
		// Get list of nearby entities
		List<Entity> bystanderEntities = npc.getBukkitEntity().getNearbyEntities(Setting.CHAT_RANGE.asDouble(), Setting.CHAT_RANGE.asDouble(), Setting.CHAT_RANGE.asDouble());
		for (Entity bystander : bystanderEntities) 
			// Continue if a LivingEntity, which is compatible with TalkableEntity
			if (bystander instanceof LivingEntity) {
				// Exclude Targets
				if (tongue.isTargeted()) {
					for (Talkable target : tongue.getRecipients())
						if (target.getEntity() == bystander) continue;
				} else
				// Found a nearby LivingEntity, make it Talkable and talkNear it
				new TalkableEntity((LivingEntity) bystander).talkNear(tongue, text, this);
			}
	}
	
	
	@Override
	public String getName() {
		return VOCAL_CHORD_NAME;
	}

}
