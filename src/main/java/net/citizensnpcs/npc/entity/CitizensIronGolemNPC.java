package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;

import net.minecraft.server.EntityIronGolem;
import net.minecraft.server.World;

//import org.bukkit.entity.IronGolem;

public class CitizensIronGolemNPC extends CitizensMobNPC {

    public CitizensIronGolemNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityIronGolemNPC.class);
    }

    /*@Override
    public IronGolem getBukkitEntity() {
        return (IronGolem) getHandle().getBukkitEntity();
    }*/

    public static class EntityIronGolemNPC extends EntityIronGolem {

        public EntityIronGolemNPC(World world) {
            super(world);
        }

        @Override
        public void d_() {
        }
    }
}