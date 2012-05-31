package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.abstraction.World;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;

public class CitizensMushroomCowNPC extends CitizensMobNPC {

    public CitizensMushroomCowNPC(int id, String name) {
        super(id, name, EntityMushroomCowNPC.class);
    }

    @Override
    public MushroomCow getBukkitEntity() {
        return (MushroomCow) getHandle().getBukkitEntity();
    }

    public static class EntityMushroomCowNPC extends EntityMushroomCow implements NPCHandle {
        private final CitizensNPC npc;

        public EntityMushroomCowNPC(World world) {
            this(world, null);
        }

        public EntityMushroomCowNPC(World world, NPC npc) {
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