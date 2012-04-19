package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntitySpider;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Spider;

public class CitizensSpiderNPC extends CitizensMobNPC {

    public CitizensSpiderNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySpiderNPC.class);
    }

    @Override
    public Spider getBukkitEntity() {
        return (Spider) getHandle().getBukkitEntity();
    }

    public static class EntitySpiderNPC extends EntitySpider implements NPCHandle {
        private final CitizensNPC npc;

        public EntitySpiderNPC(World world, CitizensNPC npc) {
            super(world);
            this.npc = npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
            npc.update();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}