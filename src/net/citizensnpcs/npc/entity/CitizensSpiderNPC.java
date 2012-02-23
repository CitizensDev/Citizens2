package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.Entity;
import net.minecraft.server.EntitySpider;
import net.minecraft.server.World;

import org.bukkit.entity.Spider;

public class CitizensSpiderNPC extends CitizensMobNPC {

    public CitizensSpiderNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySpiderNPC.class);
    }

    @Override
    public Spider getBukkitEntity() {
        return (Spider) getHandle().getBukkitEntity();
    }

	@Override
	public Entity getEntity() {
		return (EntitySpider) getHandle();
	}

    public static class EntitySpiderNPC extends EntitySpider {

        public EntitySpiderNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}