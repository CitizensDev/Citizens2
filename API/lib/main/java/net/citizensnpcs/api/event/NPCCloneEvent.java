package net.citizensnpcs.api.event;

import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

public class NPCCloneEvent extends NPCEvent {
    private final NPC clone;

    public NPCCloneEvent(NPC npc, NPC clone) {
        super(npc);
        this.clone = clone;
    }

    public NPC getClone() {
        return clone;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
