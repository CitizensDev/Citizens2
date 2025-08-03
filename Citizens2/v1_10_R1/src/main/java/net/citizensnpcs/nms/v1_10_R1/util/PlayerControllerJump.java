package net.citizensnpcs.nms.v1_10_R1.util;

import net.citizensnpcs.nms.v1_10_R1.entity.EntityHumanNPC;

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
        this.a.k(this.b);
        this.b = false;
    }
}
