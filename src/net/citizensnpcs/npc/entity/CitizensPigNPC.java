package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityPig;
import net.minecraft.server.World;

import org.bukkit.entity.Pig;

public class CitizensPigNPC extends CitizensMobNPC {

    public CitizensPigNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityPigNPC.class);
    }

    @Override
    public Pig getBukkitEntity() {
        return (Pig) getHandle().getBukkitEntity();
    }

	@Override
	public Entity getEntity() {
		return (EntityPig) getHandle();
	}

    public static class EntityPigNPC extends EntityPig {

        public EntityPigNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}