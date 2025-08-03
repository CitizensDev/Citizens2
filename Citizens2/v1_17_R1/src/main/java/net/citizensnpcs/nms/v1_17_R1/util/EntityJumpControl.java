package net.citizensnpcs.nms.v1_17_R1.util;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.monster.Slime;

public class EntityJumpControl extends JumpControl {
    private boolean a;
    private final LivingEntity b;

    public EntityJumpControl(LivingEntity entityinsentient) {
        super(new Slime(EntityType.SLIME, entityinsentient.level));
        this.b = entityinsentient;
    }

    @Override
    public void jump() {
        this.a = true;
    }

    @Override
    public void tick() {
        this.b.setJumping(this.a);
        this.a = false;
    }
}
