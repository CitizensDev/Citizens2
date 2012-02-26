package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.EntityGhast;
import net.minecraft.server.World;

import org.bukkit.entity.Ghast;

public class CitizensGhastNPC extends CitizensMobNPC {

    public CitizensGhastNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityGhastNPC.class);
    }

    @Override
    public Ghast getBukkitEntity() {
        return (Ghast) getHandle().getBukkitEntity();
    }

    public static class EntityGhastNPC extends EntityGhast {

        public EntityGhastNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}