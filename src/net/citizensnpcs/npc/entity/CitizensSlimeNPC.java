package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntitySlimeNPC;

public class CitizensSlimeNPC extends CitizensNPC {

    public CitizensSlimeNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySlimeNPC.class);
    }
}