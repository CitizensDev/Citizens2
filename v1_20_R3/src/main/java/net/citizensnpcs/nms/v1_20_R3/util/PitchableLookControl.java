package net.citizensnpcs.nms.v1_20_R3.util;

import java.util.function.Supplier;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

public class PitchableLookControl extends LookControl {
    private final Supplier<Boolean> resetOnTick;

    public PitchableLookControl(Mob var0) {
        super(var0);
        if (var0 instanceof NPCHolder) {
            NPC npc = ((NPCHolder) var0).getNPC();
            resetOnTick = () -> npc.data().get(NPC.Metadata.RESET_PITCH_ON_TICK, false);
        } else {
            resetOnTick = () -> true;
        }
    }

    @Override
    public boolean resetXRotOnTick() {
        return resetOnTick.get();
    }
}