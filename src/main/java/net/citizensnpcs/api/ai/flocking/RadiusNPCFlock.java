package net.citizensnpcs.api.ai.flocking;

import java.util.Collection;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Entity;

import com.google.common.collect.Lists;

public class RadiusNPCFlock implements NPCFlock {
    private final double radius;

    public RadiusNPCFlock(double radius) {
        this.radius = radius;
    }

    @Override
    public Collection<NPC> getNearby(NPC npc) {
        Collection<NPC> ret = Lists.newArrayList();
        for (Entity entity : npc.getEntity().getNearbyEntities(radius, radius, radius)) {
            NPC npc2 = CitizensAPI.getNPCRegistry().getNPC(entity);
            if (npc2 != null) {
                if (!npc2.getNavigator().isNavigating())
                    continue;
                ret.add(npc2);
            }
        }
        return ret;
    }
}
