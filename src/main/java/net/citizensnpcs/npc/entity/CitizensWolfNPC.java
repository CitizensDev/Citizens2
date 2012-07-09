package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.minecraft.server.EntityWolf;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Wolf;

public class CitizensWolfNPC extends CitizensMobNPC {

    public CitizensWolfNPC(int id, String name) {
        super(id, name, EntityWolfNPC.class);
    }

    @Override
    public Wolf getBukkitEntity() {
        return (Wolf) getHandle().getBukkitEntity();
    }

    public static class EntityWolfNPC extends EntityWolf implements NPCHolder {
        private final CitizensNPC npc;

        public EntityWolfNPC(World world) {
            this(world, null);
        }

        public EntityWolfNPC(World world, NPC npc) {
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
            if (npc != null)
                npc.update();
        }
    }
}