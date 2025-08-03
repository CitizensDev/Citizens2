package net.citizensnpcs;

import org.bukkit.Location;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.ChunkCoord;

public class NPCNeedsRespawnEvent extends NPCEvent {
    private final ChunkCoord coord;

    public NPCNeedsRespawnEvent(NPC npc, ChunkCoord at) {
        super(npc);
        coord = at;
    }

    public NPCNeedsRespawnEvent(NPC npc, Location at) {
        this(npc, new ChunkCoord(at));
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public ChunkCoord getSpawnLocation() {
        return coord;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static HandlerList handlers = new HandlerList();
}
