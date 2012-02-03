package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntitySpiderNPC;

public class CitizensSpiderNPC extends CitizensNPC {

    public CitizensSpiderNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySpiderNPC.class);
    }
}