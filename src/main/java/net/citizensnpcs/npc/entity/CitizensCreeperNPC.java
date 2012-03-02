package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;

import net.minecraft.server.EntityCreeper;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Creeper;

public class CitizensCreeperNPC extends CitizensMobNPC {

    public CitizensCreeperNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityCreeperNPC.class);
    }

    @Override
    public Creeper getBukkitEntity() {
        return (Creeper) getHandle().getBukkitEntity();
    }

    public static class EntityCreeperNPC extends EntityCreeper {

        public EntityCreeperNPC(World world) {
            super(world);
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
        }
    }
}