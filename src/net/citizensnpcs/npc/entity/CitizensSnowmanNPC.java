package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.Entity;
import net.minecraft.server.EntitySnowman;
import net.minecraft.server.World;

import org.bukkit.entity.Snowman;

public class CitizensSnowmanNPC extends CitizensMobNPC {

    public CitizensSnowmanNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySnowmanNPC.class);
    }

    @Override
    public Snowman getBukkitEntity() {
        return (Snowman) getHandle().getBukkitEntity();
    }

	@Override
	public Entity getEntity() {
		return (EntitySnowman) getHandle();
	}

    public static class EntitySnowmanNPC extends EntitySnowman {

        public EntitySnowmanNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}