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
     * Called when talking near this Talkable Entity to provide a universal method to getting an event/output.
     *
     * @param context
     *            the Speech context
     * @param message
     *            The message to send
     *
     */
    public void talkNear(SpeechContext context, String message);

    /**
     * Called when talking to this Talkable Entity to provide a universal method to getting an event/output.
     *
     * @param context
     *            the Speech context
     * @param message
     *            The message to send
     */
    public void talkTo(SpeechContext context, String message);

}
