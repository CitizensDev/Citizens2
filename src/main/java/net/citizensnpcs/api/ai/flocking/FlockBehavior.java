package net.citizensnpcs.api.ai.flocking;

import java.util.Collection;

import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;

/**
 * An interface to be used with an {@link Flocker} to represent a certain type of behavior such as cohesion, alignment
 * or separation.
 */
public interface FlockBehavior {
    /**
     * Returns the displacement vector to be combined with other {@link FlockBehavior} vectors by a {@link Flocker}.
     *
     * @param nearby
     *            the set of NPCs to consider for flocking purposes
     * @return the displacement {@link Vector}
     */
    Vector getVector(NPC npc, Collection<NPC> nearby);
}
