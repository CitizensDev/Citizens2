package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityPigZombieNPC;

public class CitizensPigZombieNPC extends CitizensNPC {

    public CitizensPigZombieNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityPigZombieNPC.class);
    }
}