package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityEnderDragon;
import net.minecraft.server.World;

import org.bukkit.entity.EnderDragon;

public class CitizensEnderDragonNPC extends CitizensMobNPC {

    public CitizensEnderDragonNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityEnderDragonNPC.class);
    }

    @Override
    public EnderDragon getBukkitEntity() {
        return (EnderDragon) getHandle().getBukkitEntity();
    }

	@Override
	public Entity getEntity() {
		return (EntityEnderDragon) getHandle();
	}

    public static class EntityEnderDragonNPC extends EntityEnderDragon {

        public EntityEnderDragonNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }

        @Override
        public void d() {
        }
    }
}