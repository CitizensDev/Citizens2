package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityCaveSpiderNPC;

public class CitizensCaveSpiderNPC extends CitizensNPC {

    public CitizensCaveSpiderNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityCaveSpiderNPC.class);
    }
}