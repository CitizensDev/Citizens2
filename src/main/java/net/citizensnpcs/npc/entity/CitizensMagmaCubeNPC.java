package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.EntityMagmaCube;
import net.minecraft.server.World;

import org.bukkit.entity.MagmaCube;

public class CitizensMagmaCubeNPC extends CitizensMobNPC {

    public CitizensMagmaCubeNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityMagmaCubeNPC.class);
    }

    @Override
    public MagmaCube getBukkitEntity() {
        return (MagmaCube) getHandle().getBukkitEntity();
    }

    public static class EntityMagmaCubeNPC extends EntityMagmaCube {

        public EntityMagmaCubeNPC(World world) {
            super(world);
            setSize(3);
        }

        @Override
        public void d_() {
        }
    }
}