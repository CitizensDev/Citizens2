package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityCow;
import net.minecraft.server.World;

import org.bukkit.entity.Cow;

public class CitizensCowNPC extends CitizensMobNPC {

    public CitizensCowNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityCowNPC.class);
    }

    @Override
    public Cow getBukkitEntity() {
        return (Cow) getHandle().getBukkitEntity();
    }

	@Override
	public Entity getEntity() {
		return (EntityCow) getHandle();
	}

    public static class EntityCowNPC extends EntityCow {

        public EntityCowNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}