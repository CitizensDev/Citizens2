package net.citizensnpcs.api.ai.flocking;

import java.util.Collection;

import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;

/**
 * Implements alignment flocking with a particular weight i.e. steering a flock of NPCs in line with each other.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Flocking_(behavior)">https://en.wikipedia.org/wiki/Flocking_(behavior)</a>
 */
public class AlignmentBehavior implements FlockBehavior {
    private final double weight;

    public AlignmentBehavior(double weight) {
        this.weight = weight;
    }

    @Override
    public Vector getVector(NPC npc, Collection<NPC> nearby) {
        Vector velocities = new Vector(0, 0, 0);
        for (NPC neighbor : nearby) {
            velocities = velocities.add(neighbor.getEntity().getVelocity());
        }
        Vector desired = velocities.multiply((double) 1 / nearby.size());
        return desired.subtract(npc.getEntity().getVelocity()).multiply(weight);
    }
}
