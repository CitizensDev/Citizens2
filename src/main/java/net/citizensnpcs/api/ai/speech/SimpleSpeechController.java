package net.citizensnpcs.api.ai.speech;

import org.bukkit.Bukkit;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.event.NPCSpeechEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Speech;

/**
 * Simple implementation of {@link SpeechController} which allows a NPC to speak with any
 * registered {@link VocalChord}.
 *
 */
public class SimpleSpeechController implements SpeechController {

	NPC npc;
	
	public SimpleSpeechController(NPC npc) {
		this.npc = npc;
	}

	@Override
	public void speak(SpeechContext context) {
		speak(context, npc.getTrait(Speech.class).getDefaultVocalChord());
	}

	@Override
	public void speak(SpeechContext context, String vocalChordName) {
		context.setTalker(npc.getBukkitEntity());
		NPCSpeechEvent event = new NPCSpeechEvent(context, vocalChordName);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) return;
		CitizensAPI.getSpeechFactory().getVocalChord(event.getVocalChordName()).talk(context);		
	}
	
}