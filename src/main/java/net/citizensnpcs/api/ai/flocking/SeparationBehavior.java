package net.citizensnpcs.api.ai.flocking;

import java.util.Collection;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.util.Vector;

public class SeparationBehavior implements FlockBehavior {
    private final double weight;

    public SeparationBehavior(double weight) {
        this.weight = weight;
    }

    @Override
    public Vector getVector(NPC npc, Collection<NPC> nearby) {
        Vector steering = new Vector(0, 0, 0);
        Vector pos = npc.getEntity().getLocation().toVector();
        for (NPC neighbor : nearby) {
            if (!neighbor.isSpawned())
                continue;
            Vector repulse = pos.subtract(neighbor.getEntity().getLocation().toVector()).multiply(1.0 / 3.0);
            steering = repulse.add(steering);
        }
        return steering.multiply(weight);
    }
}
