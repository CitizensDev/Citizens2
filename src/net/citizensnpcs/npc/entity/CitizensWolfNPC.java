package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityWolfNPC;

public class CitizensWolfNPC extends CitizensNPC {

    public CitizensWolfNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityWolfNPC.class);
    }
}