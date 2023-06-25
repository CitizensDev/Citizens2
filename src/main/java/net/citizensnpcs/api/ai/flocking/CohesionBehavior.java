package net.citizensnpcs.api.ai.flocking;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;

/**
 * Implements cohesion flocking with a particular weight i.e. steering a flock of NPCs towards each other.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Flocking_(behavior)">https://en.wikipedia.org/wiki/Flocking_(behavior)</a>
 */
public class CohesionBehavior implements FlockBehavior {
    private final double weight;

    public CohesionBehavior(double weight) {
        this.weight = weight;
    }

    @Override
    public Vector getVector(NPC npc, Collection<NPC> nearby) {
        Vector positions = new Vector(0, 0, 0);
        Location cacheLoc = new Location(null, 0, 0, 0);
        for (NPC neighbor : nearby) {
            positions = positions.add(neighbor.getEntity().getLocation(cacheLoc).toVector());
        }
        Vector center = positions.multiply(1.0 / nearby.size());
        Vector temp = npc.getEntity().getLocation(cacheLoc).toVector().subtract(center);
        if (temp.length() == 0) {
            return new Vector(0, 0, 0);
        }
        return temp.normalize().multiply(weight);
    }
}
