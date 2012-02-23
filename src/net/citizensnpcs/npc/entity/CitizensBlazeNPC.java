package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityBlaze;
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

	@Override
	public Entity getEntity() {
		return (EntityBlaze) getHandle();
	}

    public static class EntityBlazeNPC extends EntityBlaze {

        public EntityBlazeNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}