package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.EntityPig;
import net.minecraft.server.World;

import org.bukkit.entity.Pig;

public class CitizensPigNPC extends CitizensMobNPC {

    public CitizensPigNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityPigNPC.class);
    }

    @Override
    public Pig getBukkitEntity() {
        return (Pig) getHandle().getBukkitEntity();
    }

    public static class EntityPigNPC extends EntityPig {

        public EntityPigNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}