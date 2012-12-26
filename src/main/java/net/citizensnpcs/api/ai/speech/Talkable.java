package net.citizensnpcs.api.ai.speech;

import org.bukkit.entity.LivingEntity;

/**
 * Talkable provides an interface for talking to Players, Entities and NPCs.
 * 
 */
public interface Talkable extends Comparable<Object> {
	
    /**
     * Gets the LivingEntity associated with this Talkable
     * 
     * @return a LivingEntity
     */
    public LivingEntity getEntity();

    /**
     * Gets the name of the Talkable LivingEntity
     * 
     * @return name
     */
    public String getName();

    /**
     * Called by a {@link VocalChord} when talking near this Talkable Entity
     * to provide a universal method to getting an event/output.
     * 
     * @param talker
     *            The {@link Talkable} entity doing the talking
     * @param text
     *            The message to talk
     * 
     */
    public void talkNear(SpeechContext context, String message, VocalChord vocalChord);
    
    /**
     * Called by a {@link VocalChord} when talking to this Talkable Entity
     * to provide a universal method to getting an event/output.
     * 
     * @param talker
     *            The {@link Talkable} entity doing the talking
     * @param message
     *            The text to talk
     * 
     */
    public void talkTo(SpeechContext context, String message, VocalChord vocalChord);
    
}
