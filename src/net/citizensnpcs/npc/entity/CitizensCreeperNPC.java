package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityCreeperNPC;

public class CitizensCreeperNPC extends CitizensNPC {

    public CitizensCreeperNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityCreeperNPC.class);
    }
}