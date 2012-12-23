package net.citizensnpcs.npc.ai.speech;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.citizensnpcs.api.ai.speech.SpeechFactory;
import net.citizensnpcs.api.ai.speech.VocalChord;

public class CitizensSpeechFactory implements SpeechFactory {

	Map<String, Class<? extends VocalChord>> registered = new HashMap<String, Class <? extends VocalChord>>();
	
	@Override
	public void register(Class<? extends VocalChord> clazz, String name) {
		registered.put(name, clazz);
	}

	@Override
	public String getVocalChordName(Class<? extends VocalChord> clazz) {
		for (Entry<String, Class<? extends VocalChord>> vocalChord : registered.entrySet())
			if (vocalChord.getValue() == clazz) return vocalChord.getKey();
		return null;
	}

	@Override
	public VocalChord getVocalChord(String name) {
		if (registered.containsKey(name.toLowerCase()))
			try {
				return registered.get(name.toLowerCase()).newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		return null;
	}

	@Override
	public VocalChord getVocalChord(Class<? extends VocalChord> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
