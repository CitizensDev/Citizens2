package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.Entity;
import net.minecraft.server.EntitySkeleton;
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

	@Override
	public Entity getEntity() {
		return (EntitySkeleton) getHandle();
	}

    public static class EntitySkeletonNPC extends EntitySkeleton {

        public EntitySkeletonNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}