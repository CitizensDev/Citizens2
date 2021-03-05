package net.citizensnpcs.api.ai.flocking;

import java.util.Collection;

import org.bukkit.entity.Entity;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

/**
 * A dynamic flock of NPCs that checks for entity NPCs within a certain block radius.
 */
public class RadiusNPCFlock implements NPCFlock {
    private Collection<NPC> cached;
    private int cacheTicks = 0;
    private final int maxCacheTicks;
    private final double radius;

    public RadiusNPCFlock(double radius) {
        this(radius, 30);
    }

    /**
     * 
     * @param radius
     *            the radius to look for nearby NPCs, in blocks @see
     *            {@link Entity#getNearbyEntities(double, double, double)}
     * @param maxCacheTicks
     *            the maximum cache ticks to cache the nearby NPC 'flock' (default 30)
     */
    public RadiusNPCFlock(double radius, int maxCacheTicks) {
        this.radius = radius;
        this.maxCacheTicks = maxCacheTicks;
    }

    @Override
    public Collection<NPC> getNearby(NPC npc) {
        if (cached != null && cacheTicks++ < maxCacheTicks) {
            return cached;
        }
        cached = null;
        cacheTicks = 0;
        Collection<NPC> ret = Lists.newArrayList();
        for (Entity entity : npc.getEntity().getNearbyEntities(radius, radius, radius)) {
            NPC npc2 = CitizensAPI.getNPCRegistry().getNPC(entity);
            if (npc2 != null && npc2.getNavigator().isNavigating()) {
                ret.add(npc2);
            }
        }
        if (maxCacheTicks <= 0) {
            return ret;
        }
        return this.cached = ret;
    }
}
