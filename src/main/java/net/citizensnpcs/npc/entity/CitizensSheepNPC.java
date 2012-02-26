package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.EntitySheep;
import net.minecraft.server.World;

import org.bukkit.entity.Sheep;

public class CitizensSheepNPC extends CitizensMobNPC {

    public CitizensSheepNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySheepNPC.class);
    }

    @Override
    public Sheep getBukkitEntity() {
        return (Sheep) getHandle().getBukkitEntity();
    }

    public static class EntitySheepNPC extends EntitySheep {

        public EntitySheepNPC(World world) {
            super(world);
        }

        @Override
        public void m_() {
        }
    }
}