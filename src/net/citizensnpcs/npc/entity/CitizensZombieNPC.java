package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.World;

import org.bukkit.entity.Zombie;

public class CitizensZombieNPC extends CitizensMobNPC {

    public CitizensZombieNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityZombieNPC.class);
    }

    @Override
    public Zombie getBukkitEntity() {
        return (Zombie) getHandle().getBukkitEntity();
    }

    public static class EntityZombieNPC extends EntityZombie {

        public EntityZombieNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}