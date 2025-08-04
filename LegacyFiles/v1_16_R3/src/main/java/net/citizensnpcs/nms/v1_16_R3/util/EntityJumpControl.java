package net.citizensnpcs.nms.v1_16_R3.util;

import net.minecraft.server.v1_16_R3.ControllerJump;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntitySlime;
import net.minecraft.server.v1_16_R3.EntityTypes;

public class EntityJumpControl extends ControllerJump {
    private boolean a;
    private final EntityLiving b;

    public EntityJumpControl(EntityLiving entityinsentient) {
        super(new EntitySlime(EntityTypes.SLIME, entityinsentient.world));
        this.b = entityinsentient;
    }

    @Override
    public void b() {
        this.b.setJumping(this.a);
        this.a = false;
    }

    @Override
    public void jump() {
        this.a = true;
    }
}
