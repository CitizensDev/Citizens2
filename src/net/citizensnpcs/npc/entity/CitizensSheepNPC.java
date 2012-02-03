package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntitySheepNPC;

public class CitizensSheepNPC extends CitizensNPC {

    public CitizensSheepNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySheepNPC.class);
    }
}