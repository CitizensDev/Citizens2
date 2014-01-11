package net.citizensnpcs.api.ai.flocking;

import java.util.Collection;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.util.Vector;

public class AlignmentBehavior implements FlockBehavior {
    private final double weight;

    public AlignmentBehavior(double weight) {
        this.weight = weight;
    }

    @Override
    public Vector getVector(NPC npc, Collection<NPC> nearby) {
        Vector velocities = new Vector(0, 0, 0);
        for (NPC neighbor : nearby) {
            if (!neighbor.isSpawned())
                continue;
            velocities = velocities.add(neighbor.getEntity().getVelocity());
        }
        Vector desired = velocities.multiply((double) 1 / nearby.size());
        return desired.subtract(npc.getEntity().getVelocity()).multiply(weight);
    }
}
