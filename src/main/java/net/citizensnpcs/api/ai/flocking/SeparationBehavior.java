package net.citizensnpcs.api.ai.flocking;

import java.util.Collection;

import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;

public class SeparationBehavior implements FlockBehavior {
    private final double weight;

    public SeparationBehavior(double weight) {
        this.weight = weight;
    }

    @Override
    public Vector getVector(NPC npc, Collection<NPC> nearby) {
        Vector steering = new Vector(0, 0, 0);
        Vector pos = npc.getEntity().getLocation().toVector();
        int c = 0;
        for (NPC neighbor : nearby) {
            if (!neighbor.isSpawned())
                continue;
            double dist = neighbor.getEntity().getLocation().toVector().distance(pos);
            Vector repulse = pos.subtract(neighbor.getEntity().getLocation().toVector()).normalize()
                    .divide(new Vector(dist, dist, dist));
            steering = repulse.add(steering);
            c++;
        }
        steering = steering.divide(new Vector(c, c, c));
        return steering.subtract(npc.getEntity().getVelocity()).multiply(weight);
    }
}
