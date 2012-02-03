package net.citizensnpcs.npc.entity;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.resource.lib.entity.EntitySkeletonNPC;

public class CitizensSkeletonNPC extends CitizensNPC {

    public CitizensSkeletonNPC(CitizensNPCManager manager, int id, String name) {
        super(manager, id, name, EntitySkeletonNPC.class);
    }
}