package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityGiantNPC;

public class CitizensGiantNPC extends CitizensNPC {

    public CitizensGiantNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityGiantNPC.class);
    }
}