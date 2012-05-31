package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.abstraction.World;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;

public class CitizensSlimeNPC extends CitizensMobNPC {

    public CitizensSlimeNPC(int id, String name) {
        super(id, name, EntitySlimeNPC.class);
    }

    @Override
    public Slime getBukkitEntity() {
        return (Slime) getHandle().getBukkitEntity();
    }

    public static class EntitySlimeNPC extends EntitySlime implements NPCHandle {
        private final CitizensNPC npc;

        public EntitySlimeNPC(World world) {
            this(world, null);
        }

        public EntitySlimeNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            setSize(3);
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