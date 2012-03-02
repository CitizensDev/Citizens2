package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;

import net.minecraft.server.EntitySlime;
import net.minecraft.server.PathfinderGoalSelector;
import net.minecraft.server.World;

import org.bukkit.entity.Slime;

public class CitizensSlimeNPC extends CitizensMobNPC {

    public CitizensSlimeNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySlimeNPC.class);
    }

    @Override
    public Slime getBukkitEntity() {
        return (Slime) getHandle().getBukkitEntity();
    }

    public static class EntitySlimeNPC extends EntitySlime {

        public EntitySlimeNPC(World world) {
            super(world);
            setSize(3);
            goalSelector = new PathfinderGoalSelector();
            targetSelector = new PathfinderGoalSelector();
        }

        @Override
        public void d_() {
        }
    }
}