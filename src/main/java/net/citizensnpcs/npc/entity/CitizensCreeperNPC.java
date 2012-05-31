package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.abstraction.World;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;

public class CitizensCreeperNPC extends CitizensMobNPC {

    public CitizensCreeperNPC(int id, String name) {
        super(id, name, EntityCreeperNPC.class);
    }

    @Override
    public Creeper getBukkitEntity() {
        return (Creeper) getHandle().getBukkitEntity();
    }

    public static class EntityCreeperNPC extends EntityCreeper implements NPCHandle {
        private final CitizensNPC npc;

        public EntityCreeperNPC(World world) {
            this(world, null);
        }

        public EntityCreeperNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void a(EntityWeatherLighting entityweatherlighting) {
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