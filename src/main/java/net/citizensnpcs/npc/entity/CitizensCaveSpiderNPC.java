package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;

import net.minecraft.server.EntityCaveSpider;
import net.minecraft.server.World;

import org.bukkit.entity.CaveSpider;

public class CitizensCaveSpiderNPC extends CitizensMobNPC {

    public CitizensCaveSpiderNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityCaveSpiderNPC.class);
    }

    @Override
    public CaveSpider getBukkitEntity() {
        return (CaveSpider) getHandle().getBukkitEntity();
    }

    public static class EntityCaveSpiderNPC extends EntityCaveSpider {

        public EntityCaveSpiderNPC(World world) {
            super(world);
        }

        @Override
        public void d_() {
        }
    }
}