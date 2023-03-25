package net.citizensnpcs.nms.v1_18_R2.util;

import java.util.function.Supplier;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

public class PitchableLookControl extends LookControl {
    private boolean explicit = true;
    private final Supplier<Boolean> resetOnTick;

    public PitchableLookControl(Mob var0) {
        super(var0);
        if (var0 instanceof NPCHolder) {
            NPC npc = ((NPCHolder) var0).getNPC();
            resetOnTick = () -> npc.data().get(NPC.Metadata.RESET_PITCH_ON_TICK, explicit);
        } else {
            resetOnTick = () -> explicit;
        }
    }

    @Override
    public boolean resetXRotOnTick() {
        return resetOnTick.get();
    }

    public void setResetXRotOnTick(boolean val) {
        explicit = val;
    }
}