package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.EntityPigZombie;
import net.minecraft.server.World;

import org.bukkit.entity.PigZombie;

public class CitizensPigZombieNPC extends CitizensMobNPC {

    public CitizensPigZombieNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityPigZombieNPC.class);
    }

    @Override
    public PigZombie getBukkitEntity() {
        return (PigZombie) getHandle().getBukkitEntity();
    }

    public static class EntityPigZombieNPC extends EntityPigZombie {

        public EntityPigZombieNPC(World world) {
            super(world);
        }

        @Override
        public void d_() {
        }
    }
}