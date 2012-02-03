package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntityMagmaCubeNPC;

public class CitizensMagmaCubeNPC extends CitizensNPC {

    public CitizensMagmaCubeNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntityMagmaCubeNPC.class);
    }
}