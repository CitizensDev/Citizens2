package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;

import net.minecraft.server.EntityBlaze;
import net.minecraft.server.World;

import org.bukkit.entity.Blaze;

public class CitizensBlazeNPC extends CitizensMobNPC {

    public CitizensBlazeNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityBlazeNPC.class);
    }

    @Override
    public Blaze getBukkitEntity() {
        return (Blaze) getHandle().getBukkitEntity();
    }

    public static class EntityBlazeNPC extends EntityBlaze {

        public EntityBlazeNPC(World world) {
            super(world);
        }

        @Override
        public void d_() {
        }
    }
}