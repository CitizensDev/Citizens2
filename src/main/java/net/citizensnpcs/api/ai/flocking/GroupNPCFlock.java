package net.citizensnpcs.api.ai.flocking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.citizensnpcs.api.npc.NPC;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class GroupNPCFlock implements NPCFlock {
    private final List<NPC> npcs;
    private final double radius;

    public GroupNPCFlock(Iterable<NPC> npcs, double radius) {
        this.npcs = new ArrayList<NPC>();
        this.radius = radius;
    }

    @Override
    public Collection<NPC> getNearby(final NPC npc) {
        if (radius < 0)
            return npcs;
        return Collections2.filter(npcs, new Predicate<NPC>() {
            @Override
            public boolean apply(NPC input) {
                return input.getStoredLocation().distance(npc.getStoredLocation()) < radius;
            }
        });
    }

    public static GroupNPCFlock create(Iterable<NPC> npcs) {
        return new GroupNPCFlock(npcs, -1);
    }

    public static GroupNPCFlock createWithRadius(Iterable<NPC> npcs, double radius) {
        return new GroupNPCFlock(npcs, radius);
    }
}
