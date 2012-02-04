package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
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

    public static class EntityEnderDragonNPC extends EntityEnderDragon {

        public EntityEnderDragonNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}