package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityVillagerNPC;

public class CitizensVillagerNPC extends CitizensNPC {

    public CitizensVillagerNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityVillagerNPC.class);
    }
}