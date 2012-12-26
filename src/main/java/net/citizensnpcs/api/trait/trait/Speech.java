package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.VocalChord;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

/**
 * Represents the default speech settings of an NPC.
 */
public class Speech extends Trait {
	
	@Persist("")
	private String defaultVocalChord = DEFAULT_VOCAL_CHORD;

    public Speech() {
        super("speech");
    }

    /**
     * Gets the name of the default {@link VocalChord} for this NPC.
     * 
     * @return The name of the VocalChord
     */
    public String getDefaultVocalChord() {
        return defaultVocalChord;
    }

    /**
     * Sets the name of the default {@link VocalChord} for this NPC.
     * 
     * @return The name of the VocalChord
     */
    public void setDefaultVocalChord(Class<VocalChord> clazz) {
        defaultVocalChord = CitizensAPI.getSpeechFactory().getVocalChordName(clazz);
    }
    
    @Override
    public String toString() {
        return "DefaultVocalChord{" + defaultVocalChord + "}";
    }

    public static final String DEFAULT_VOCAL_CHORD = "chat";
}