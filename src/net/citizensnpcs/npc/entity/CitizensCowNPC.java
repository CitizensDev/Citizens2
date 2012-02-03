package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityCowNPC;

public class CitizensCowNPC extends CitizensNPC {

    public CitizensCowNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityCowNPC.class);
    }
}