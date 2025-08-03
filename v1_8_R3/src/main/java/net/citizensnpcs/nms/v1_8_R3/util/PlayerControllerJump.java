package net.citizensnpcs.nms.v1_8_R3.util;

import net.citizensnpcs.nms.v1_8_R3.entity.EntityHumanNPC;

public class PlayerControllerJump {
    private final EntityHumanNPC a;
    private boolean b;

    public PlayerControllerJump(EntityHumanNPC entityinsentient) {
        this.a = entityinsentient;
    }

    public void a() {
        this.b = true;
    }

    public void b() {
        this.a.i(this.b);
        this.b = false;
    }
}
