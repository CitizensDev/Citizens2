package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntityBlaze;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Blaze;

public class CitizensBlazeNPC extends CitizensMobNPC {

    public CitizensBlazeNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityBlazeNPC.class);
    }

    @Override
    public Blaze getBukkitEntity() {
        return (Blaze) getHandle().getBukkitEntity();
    }

    public static class EntityBlazeNPC extends EntityBlaze implements NPCHandle {
        private final NPC npc;

        @Override
        public NPC getNPC() {
            return this.npc;
        }

        public EntityBlazeNPC(World world, NPC npc) {
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