package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityGhastNPC;

public class CitizensGhastNPC extends CitizensNPC {

    public CitizensGhastNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityGhastNPC.class);
    }
}