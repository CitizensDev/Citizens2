package net.citizensnpcs.api.ai.flocking;

import java.util.Collection;

import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;

/**
 * Implements separation flocking with a particular weight i.e. steering a flock of NPCs away from each other.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Flocking_(behavior)">https://en.wikipedia.org/wiki/Flocking_(behavior)</a>
 */
public class SeparationBehavior implements FlockBehavior {
    private double separation = 0.5;
    private final double weight;

    public SeparationBehavior(double weight) {
        this.weight = weight;
    }

    public SeparationBehavior(double weight, double separation) {
        this.separation = separation;
        this.weight = weight;
    }

    @Override
    public Vector getVector(NPC npc, Collection<NPC> nearby) {
        Vector steering = new Vector(0, 0, 0);
        Vector pos = npc.getEntity().getLocation().toVector();
        int count = 0;
        for (NPC neighbor : nearby) {
            Vector diff = pos.subtract(neighbor.getEntity().getLocation().toVector()).setY(0);
            double dist = diff.length();
            if (dist > separation || dist == 0) {
                continue;
            }
            steering = steering.add(diff.normalize().multiply(1 / (dist * 50)));
            count++;
        }
        count = Math.max(1, count);
        steering = steering.divide(new Vector(count, count, count));
        return steering.setY(0).multiply(weight);
    }
}
