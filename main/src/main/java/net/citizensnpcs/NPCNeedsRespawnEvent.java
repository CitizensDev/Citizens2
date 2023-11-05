package net.citizensnpcs;

import org.bukkit.Location;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.npc.NPC;

public class NPCNeedsRespawnEvent extends NPCEvent {
    private final Location spawn;

    public NPCNeedsRespawnEvent(NPC npc, Location at) {
        super(npc);
        spawn = at;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Location getSpawnLocation() {
        return spawn.clone();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static HandlerList handlers = new HandlerList();
}
