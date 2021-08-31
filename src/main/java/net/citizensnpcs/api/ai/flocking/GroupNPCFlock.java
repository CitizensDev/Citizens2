package net.citizensnpcs.api.ai.flocking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.citizensnpcs.api.npc.NPC;

/**
 * Defines a static flock of NPCs with an optional radius. If the radius is positive then NPCs will only be considered
 * part of the flock if they are within the base NPC's radius currently.
 */
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
        return npcs.stream().filter(new Predicate<NPC>() {
            @Override
            public boolean test(NPC input) {
                return input.getStoredLocation().distance(npc.getStoredLocation()) < radius;
            }
        }).collect(Collectors.<NPC> toList());
    }

    public List<NPC> getNPCs() {
        return npcs;
    }

    public static GroupNPCFlock create(Iterable<NPC> npcs) {
        return new GroupNPCFlock(npcs, -1);
    }

    public static GroupNPCFlock createWithRadius(Iterable<NPC> npcs, double radius) {
        return new GroupNPCFlock(npcs, radius);
    }
}
