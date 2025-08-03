package net.citizensnpcs.api.ai.flocking;

import java.util.Collection;

import net.citizensnpcs.api.npc.NPC;

/**
 * Represents a 'flock' of NPCs to be used as input to a {@link Flocker}.
 *
 * @see RadiusNPCFlock
 * @see GroupNPCFlock
 */
public interface NPCFlock {
    /**
     * Returns the NPCs to be considered part of a flock.
     */
    public Collection<NPC> getNearby(NPC npc);
}
