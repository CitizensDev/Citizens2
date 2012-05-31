package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.abstraction.World;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;

public class CitizensSilverfishNPC extends CitizensMobNPC {

    public CitizensSilverfishNPC(int id, String name) {
        super(id, name, EntitySilverfishNPC.class);
    }

    @Override
    public Silverfish getBukkitEntity() {
        return (Silverfish) getHandle().getBukkitEntity();
    }

    public static class EntitySilverfishNPC extends EntitySilverfish implements NPCHandle {
        private final CitizensNPC npc;

        public EntitySilverfishNPC(World world) {
            this(world, null);
        }

        public EntitySilverfishNPC(World world, NPC npc) {
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