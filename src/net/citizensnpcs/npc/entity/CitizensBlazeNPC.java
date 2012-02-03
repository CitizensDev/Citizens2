package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityBlazeNPC;

public class CitizensBlazeNPC extends CitizensNPC {

    public CitizensBlazeNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityBlazeNPC.class);
    }
}