package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntitySilverfishNPC;

public class CitizensSilverfishNPC extends CitizensNPC {

    public CitizensSilverfishNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySilverfishNPC.class);
    }
}