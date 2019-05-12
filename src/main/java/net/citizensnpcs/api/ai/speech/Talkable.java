package net.citizensnpcs.api.ai.speech;

import org.bukkit.entity.Entity;

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
    public Entity getEntity();

    /**
     * Gets the name of the Talkable LivingEntity
     *
     * @return name
     */
    public String getName();

    /**
     * Called by a {@link VocalChord} when talking near this Talkable Entity to provide a universal method to getting an
     * event/output.
     *
     * @param context
     *            the Speech context
     * @param message
     *            The message to send
     * @param vocalChord
     *            The chord to use
     *
     */
    public void talkNear(SpeechContext context, String message, VocalChord vocalChord);

    /**
     * Called by a {@link VocalChord} when talking to this Talkable Entity to provide a universal method to getting an
     * event/output.
     * 
     * @param context
     *            the Speech context
     * @param message
     *            The message to send
     * @param vocalChord
     *            The chord to use
     */
    public void talkTo(SpeechContext context, String message, VocalChord vocalChord);

}
