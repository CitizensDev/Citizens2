package net.citizensnpcs.nms.v1_16_R3.util;

import java.util.function.Supplier;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.minecraft.server.v1_16_R3.ControllerLook;
import net.minecraft.server.v1_16_R3.EntityInsentient;

public class PitchableLookControl extends ControllerLook {
    private boolean explicit = true;
    private final Supplier<Boolean> resetOnTick;

    public PitchableLookControl(EntityInsentient var0) {
        super(var0);
        if (var0 instanceof NPCHolder) {
            NPC npc = ((NPCHolder) var0).getNPC();
            resetOnTick = () -> npc.data().get(NPC.Metadata.RESET_PITCH_ON_TICK, explicit);
        } else {
            resetOnTick = () -> explicit;
        }
    }

    @Override
    public boolean b() {
        return resetOnTick.get();
    }

    public void setResetXRotOnTick(boolean val) {
        explicit = val;
    }
}