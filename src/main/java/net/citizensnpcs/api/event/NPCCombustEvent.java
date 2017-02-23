package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityCombustEvent;

public class NPCCombustEvent extends NPCEvent implements Cancellable {
    private boolean cancelled;
    private final EntityCombustEvent event;

    public NPCCombustEvent(EntityCombustEvent event, NPC npc) {
        super(npc);
        this.event = event;
    }

    /**
     * @return the amount of time (in seconds) the combustee should be alight for
     */
    public int getDuration() {
        return event.getDuration();
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

    /**
     * The number of seconds the combustee should be alight for.
     * <p />
     * This value will only ever increase the combustion time, not decrease existing combustion times.
     * 
     * @param duration
     *            the time in seconds to be alight for.
     */
    public void setDuration(int duration) {
        event.setDuration(duration);
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
