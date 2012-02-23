package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.Entity;
import net.minecraft.server.EntitySquid;
import net.minecraft.server.World;

import org.bukkit.entity.Squid;

public class CitizensSquidNPC extends CitizensMobNPC {

    public CitizensSquidNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySquidNPC.class);
    }

    @Override
    public Squid getBukkitEntity() {
        return (Squid) getHandle().getBukkitEntity();
    }

	@Override
	public Entity getEntity() {
		return (EntitySquid) getHandle();
	}

    public static class EntitySquidNPC extends EntitySquid {

        public EntitySquidNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}