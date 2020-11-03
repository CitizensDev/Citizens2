package net.citizensnpcs.nms.v1_16_R3.util;

import net.citizensnpcs.nms.v1_16_R3.entity.EntityHumanNPC;

public class PlayerControllerJump {
    private boolean a;
    private final EntityHumanNPC b;

    public PlayerControllerJump(EntityHumanNPC entityinsentient) {
        this.b = entityinsentient;
    }

    public void b() {
        this.b.setJumping(this.a);
        this.a = false;
    }

    public void jump() {
        this.a = true;
    }
}
