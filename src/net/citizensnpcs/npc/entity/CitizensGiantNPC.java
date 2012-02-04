package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.EntityGiantZombie;
import net.minecraft.server.World;

import org.bukkit.entity.Giant;

public class CitizensGiantNPC extends CitizensMobNPC {

    public CitizensGiantNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityGiantNPC.class);
    }

    @Override
    public Giant getBukkitEntity() {
        return (Giant) getHandle().getBukkitEntity();
    }

    public static class EntityGiantNPC extends EntityGiantZombie {

        public EntityGiantNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}