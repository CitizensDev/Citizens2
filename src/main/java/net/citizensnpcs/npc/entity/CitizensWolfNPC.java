package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityWolf;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Wolf;

public class CitizensWolfNPC extends CitizensMobNPC {

    public CitizensWolfNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityWolfNPC.class);
    }

    @Override
    public Wolf getBukkitEntity() {
        return (Wolf) getHandle().getBukkitEntity();
    }

    public static class EntityWolfNPC extends EntityWolf implements NPCHandle {
        private final NPC npc;

        @Override
        public NPC getNPC() {
            return this.npc;
        }

        public EntityWolfNPC(World world, NPC npc) {
            super(world);
            this.npc = npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
        }
    }
}