package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHandle;

import org.bukkit.entity.Blaze;

public class CitizensBlazeNPC extends CitizensMobNPC {

    public CitizensBlazeNPC(int id, String name) {
        super(id, name, EntityBlazeNPC.class);
    }

    @Override
    public Blaze getBukkitEntity() {
        return (Blaze) getHandle().getBukkitEntity();
    }

    public static class EntityBlazeNPC extends EntityBlaze implements NPCHandle {
        private final CitizensNPC npc;

        public EntityBlazeNPC(World world) {
            this(world, null);
        }

        public EntityBlazeNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
            if (npc != null)
                npc.update();
            else
                super.d_();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}