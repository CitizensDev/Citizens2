package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPCManager;
import net.minecraft.server.EntitySlime;
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
        }

        @Override
        public void m_() {
        }
    }
}