package net.citizensnpcs;

import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Location;
import org.bukkit.event.HandlerList;

public class NPCNeedsRespawnEvent extends NPCEvent {
    private final Location spawn;

    public NPCNeedsRespawnEvent(NPC npc, Location at) {
        super(npc);
        this.spawn = at;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Location getSpawnLocation() {
        return spawn;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
