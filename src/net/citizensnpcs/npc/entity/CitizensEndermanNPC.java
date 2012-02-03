package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityEndermanNPC;

public class CitizensEndermanNPC extends CitizensNPC {

    public CitizensEndermanNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityEndermanNPC.class);
    }
}