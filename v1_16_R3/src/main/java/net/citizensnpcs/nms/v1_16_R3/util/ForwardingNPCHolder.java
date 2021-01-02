package net.citizensnpcs.nms.v1_16_R3.util;

import org.bukkit.entity.Entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;

public interface ForwardingNPCHolder extends NPCHolder, Entity {
    @Override
    default NPC getNPC() {
        return ((NPCHolder) NMSImpl.getHandle(this)).getNPC();
    }
}
