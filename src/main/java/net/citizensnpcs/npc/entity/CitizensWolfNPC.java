package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.EntityWolf;
import net.minecraft.server.World;

import org.bukkit.entity.Wolf;

public class CitizensWolfNPC extends CitizensMobNPC {

    public CitizensWolfNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityWolfNPC.class);
    }

    @Override
    public Wolf getBukkitEntity() {
        return (Wolf) getHandle().getBukkitEntity();
    }

    public static class EntityWolfNPC extends EntityWolf {

        public EntityWolfNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}