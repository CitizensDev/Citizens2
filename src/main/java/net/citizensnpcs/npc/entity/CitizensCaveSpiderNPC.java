package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.minecraft.server.EntityCaveSpider;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.CaveSpider;

public class CitizensCaveSpiderNPC extends CitizensMobNPC {

    public CitizensCaveSpiderNPC(int id, String name) {
        super(id, name, EntityCaveSpiderNPC.class);
    }

    @Override
    public CaveSpider getBukkitEntity() {
        return (CaveSpider) getHandle().getBukkitEntity();
    }

    public static class EntityCaveSpiderNPC extends EntityCaveSpider implements NPCHolder {
        private final CitizensNPC npc;

        public EntityCaveSpiderNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public void z_() {
            super.z_();
            npc.update();
        }
    }
}