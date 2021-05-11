package net.citizensnpcs.api.ai.flocking;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.util.Vector;

import com.google.common.collect.Collections2;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;

/**
 * Implements a simple flocking controller as a {@link Runnable}. This should be run every tick, for example as part of
 * an overridden {@link Trait#run}.
 *
 * @see NPCFlock
 * @see FlockBehavior
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Flocking_(behavior)">https://en.wikipedia.org/wiki/Flocking_(behavior)</a>
 */
public class Flocker implements Runnable {
    private final List<FlockBehavior> behaviors;
    private final NPCFlock flock;
    private double maxForce = 1.5;
    private final NPC npc;

    public Flocker(NPC npc, NPCFlock flock, FlockBehavior... behaviors) {
        this.npc = npc;
        this.flock = flock;
        this.behaviors = Arrays.asList(behaviors);
    }

    @Override
    public void run() {
        Collection<NPC> nearby = Collections2.filter(flock.getNearby(npc), npc -> npc.isSpawned());
        if (nearby.isEmpty())
            return;
        Vector base = new Vector(0, 0, 0);
        for (FlockBehavior behavior : behaviors) {
            base.add(behavior.getVector(npc, nearby));
        }
        base = clip(maxForce, base);
        npc.getEntity().setVelocity(npc.getEntity().getVelocity().add(base));
    }

    /**
     * Sets the maximum length of the resultant flocking vector.
     *
     * @param maxForce
     *            the new maximum length
     */
    public void setMaxForce(double maxForce) {
        if (maxForce == 0)
            throw new IllegalArgumentException();
        this.maxForce = maxForce;
    }

    private static Vector clip(double max, Vector vector) {
        if (vector.length() > max) {
            return vector.normalize().multiply(max);
        }
        return vector;
    }

    /**
     * Sample weight indicating high amount of influence from flocking.
     */
    public static double HIGH_INFLUENCE = 1.0 / 20.0;
    /**
     * Sample weight indicating low amount of influence from flocking.
     */
    public static double LOW_INFLUENCE = 1.0 / 200.0;
}
