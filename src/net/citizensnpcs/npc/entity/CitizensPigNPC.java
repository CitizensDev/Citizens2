package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityPigNPC;

public class CitizensPigNPC extends CitizensNPC {

    public CitizensPigNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityPigNPC.class);
    }
}