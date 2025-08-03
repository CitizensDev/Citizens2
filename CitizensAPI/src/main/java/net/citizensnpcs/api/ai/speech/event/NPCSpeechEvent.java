package net.citizensnpcs.api.ai.speech.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.event.NPCEvent;

/**
 * Represents an event where an NPC speaks using /npc speak. 
 */
public class NPCSpeechEvent extends NPCEvent implements Cancellable {
    private boolean cancelled = false;
    private final SpeechContext context;

    public NPCSpeechEvent(SpeechContext context) {
        super(CitizensAPI.getNPCRegistry().getNPC(context.getTalker().getEntity()));
        this.context = context;
    }
 
    public SpeechContext getContext() {
        return context;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}