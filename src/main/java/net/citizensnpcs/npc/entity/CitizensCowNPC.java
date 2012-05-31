package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.abstraction.World;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;

public class CitizensCowNPC extends CitizensMobNPC {

    public CitizensCowNPC(int id, String name) {
        super(id, name, EntityCowNPC.class);
    }

    @Override
    public Cow getBukkitEntity() {
        return (Cow) getHandle().getBukkitEntity();
    }

    public static class EntityCowNPC extends EntityCow implements NPCHandle {
        private final CitizensNPC npc;

        public EntityCowNPC(World world) {
            this(world, null);
        }

        public EntityCowNPC(World world, NPC npc) {
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