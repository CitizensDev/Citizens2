package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.ai.NPCHandle;
import net.minecraft.server.EntitySkeleton;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Skeleton;

public class CitizensSkeletonNPC extends CitizensMobNPC {

    public CitizensSkeletonNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySkeletonNPC.class);
    }

    @Override
    public Skeleton getBukkitEntity() {
        return (Skeleton) getHandle().getBukkitEntity();
    }

    public static class EntitySkeletonNPC extends EntitySkeleton implements NPCHandle {
        private final NPC npc;

        @Override
        public NPC getNPC() {
            return npc;
        }

        public EntitySkeletonNPC(World world, NPC npc) {
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