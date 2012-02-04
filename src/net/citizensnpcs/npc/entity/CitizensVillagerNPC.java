package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.EntityVillager;
import net.minecraft.server.World;

import org.bukkit.entity.Villager;

public class CitizensVillagerNPC extends CitizensMobNPC {

    public CitizensVillagerNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityVillagerNPC.class);
    }

    @Override
    public Villager getBukkitEntity() {
        return (Villager) getHandle().getBukkitEntity();
    }

    public static class EntityVillagerNPC extends EntityVillager {

        public EntityVillagerNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}