package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntitySnowmanNPC;

public class CitizensSnowmanNPC extends CitizensNPC {

    public CitizensSnowmanNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySnowmanNPC.class);
    }
}