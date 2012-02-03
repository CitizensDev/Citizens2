package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntitySquidNPC;

public class CitizensSquidNPC extends CitizensNPC {

    public CitizensSquidNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySquidNPC.class);
    }
}