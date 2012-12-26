package net.citizensnpcs.api.ai.speech;

public interface VocalChord {
    /**
     * Called when an NPC's {@link SpeechController} needs to output some text to a
     * {@link Talkable} entity.
     * 
     * @param context
     * 			The {@link SpeechContext} with talk information
     * 
     */
    public void talk(SpeechContext context);
    
    /**
     * Returns the name of the vocal chord used in the registration process.
     * 
     * @return name of the VocalChord
     */
    public String getName();

}
