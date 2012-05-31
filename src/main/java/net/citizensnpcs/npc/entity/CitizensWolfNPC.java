package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.abstraction.World;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;

public class CitizensWolfNPC extends CitizensMobNPC {

    public CitizensWolfNPC(int id, String name) {
        super(id, name, EntityWolfNPC.class);
    }

    @Override
    public Wolf getBukkitEntity() {
        return (Wolf) getHandle().getBukkitEntity();
    }

    public static class EntityWolfNPC extends EntityWolf implements NPCHandle {
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
        public void z_() {
            super.z_();
            if (npc != null)
                npc.update();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}