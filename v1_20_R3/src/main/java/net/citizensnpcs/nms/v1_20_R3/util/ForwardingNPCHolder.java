package net.citizensnpcs.nms.v1_20_R3.util;

import org.bukkit.entity.Entity;

import com.google.common.base.Joiner;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.NPCHolder;

public interface ForwardingNPCHolder extends NPCHolder, Entity {
    @Override
    default NPC getNPC() {
        net.minecraft.world.entity.Entity handle = NMSImpl.getHandle(this);
        if (!(handle instanceof NPCHolder)) {
            Messaging.idebug(
                    () -> Joiner.on(' ').join("ForwardingNPCHolder with an improper bukkit entity", this, handle));
            return null;
        }
        return ((NPCHolder) handle).getNPC();
    }
}
