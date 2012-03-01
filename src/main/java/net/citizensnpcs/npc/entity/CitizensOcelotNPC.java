package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensMobNPC;
import net.citizensnpcs.npc.CitizensNPCManager;

import net.minecraft.server.EntityOcelot;
import net.minecraft.server.World;

//import org.bukkit.entity.Ocelot;

public class CitizensOcelotNPC extends CitizensMobNPC {

    public CitizensOcelotNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityOcelotNPC.class);
    }

    /*@Override
    public Ocelot getBukkitEntity() {
        return (Ocelot) getHandle().getBukkitEntity();
    }*/

    public static class EntityOcelotNPC extends EntityOcelot {

        public EntityOcelotNPC(World world) {
            super(world);
        }

        @Override
        public void d_() {
        }
    }
}