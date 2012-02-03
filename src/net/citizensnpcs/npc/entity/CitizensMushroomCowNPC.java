package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityMushroomCowNPC;

public class CitizensMushroomCowNPC extends CitizensNPC {

    public CitizensMushroomCowNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityMushroomCowNPC.class);
    }
}