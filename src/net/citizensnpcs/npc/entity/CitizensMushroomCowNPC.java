package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityMushroomCow;
import net.minecraft.server.World;

import org.bukkit.entity.MushroomCow;

public class CitizensMushroomCowNPC extends CitizensMobNPC {

    public CitizensMushroomCowNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityMushroomCowNPC.class);
    }

    @Override
    public MushroomCow getBukkitEntity() {
        return (MushroomCow) getHandle().getBukkitEntity();
    }

	@Override
	public Entity getEntity() {
		return (EntityMushroomCow) getHandle();
	}

    public static class EntityMushroomCowNPC extends EntityMushroomCow {

        public EntityMushroomCowNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}