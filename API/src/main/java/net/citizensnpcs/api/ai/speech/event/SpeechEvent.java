package net.citizensnpcs.api.ai.speech.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.Talkable;

/**
 * Represents an event where a {@link Talkable} entity speaks at/near a {@link Talkable} entity.
 *
 */
public class SpeechEvent extends Event implements Cancellable {
    private boolean cancelled = false;
    SpeechContext context;
    String message;
    Talkable target;

    public SpeechEvent(Talkable target, SpeechContext context, String message) {
        this.target = target;
        this.context = context;
        this.message = message;
    }

    /**
     * Gets the {@link SpeechContext} associated with the SpeechEvent.
     *
     * @return the SpeechContext
     */
    public SpeechContext getContext() {
        return context;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * The final message to be sent to the bystander. Note: This may differ from the message contained in the
     * SpeechContext, as formatting may have occurred.
     *
     * @return the message to be sent to the {@link Talkable} bystander.
     */
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Sets the message to be sent to the bystander. Note: This may differ from the message contained in the
     * SpeechContext, as formatting may have occurred.
     */
    public void setMessage(String formattedMessage) {
        this.message = formattedMessage;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}