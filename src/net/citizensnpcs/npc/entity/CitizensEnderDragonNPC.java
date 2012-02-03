package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityEnderDragonNPC;

public class CitizensEnderDragonNPC extends CitizensNPC {

    public CitizensEnderDragonNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityEnderDragonNPC.class);
    }
}