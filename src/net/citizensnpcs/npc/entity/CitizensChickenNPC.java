package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityChicken;
import net.minecraft.server.World;

import org.bukkit.entity.Chicken;

public class CitizensChickenNPC extends CitizensMobNPC {

    public CitizensChickenNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityChickenNPC.class);
    }

    @Override
    public Chicken getBukkitEntity() {
        return (Chicken) getHandle().getBukkitEntity();
    }

	@Override
	public Entity getEntity() {
		return (EntityChicken) getHandle();
	}

    public static class EntityChickenNPC extends EntityChicken {

        public EntityChickenNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}