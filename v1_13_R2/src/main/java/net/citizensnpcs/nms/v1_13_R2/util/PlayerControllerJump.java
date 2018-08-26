package net.citizensnpcs.nms.v1_13_R2.util;

import net.citizensnpcs.nms.v1_13_R2.entity.EntityHumanNPC;

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
        this.a.o(this.b);
        this.b = false;
    }
}
