package net.citizensnpcs.nms.v1_17_R1.util;

import java.util.Random;

import net.citizensnpcs.nms.v1_17_R1.entity.EntityHumanNPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Slime;

public class PlayerMoveControl extends MoveControl {
    protected LivingEntity a;
    protected double b;
    protected double c;
    protected double d;
    protected double e;
    protected boolean f;
    private int h;

    public PlayerMoveControl(LivingEntity entityinsentient) {
        super(entityinsentient instanceof Mob ? (Mob) entityinsentient
                : new Slime(EntityType.SLIME, entityinsentient.level));
        this.a = entityinsentient;
        this.b = entityinsentient.getX();
        this.c = entityinsentient.getY();
        this.d = entityinsentient.getZ();
    }

    protected int cg() {
        return new Random().nextInt(20) + 10;
    }

    @Override
    public double getSpeedModifier() {
        return this.e;
    }

    @Override
    public double getWantedX() {
        return this.b;
    }

    @Override
    public double getWantedY() {
        return this.c;
    }

    @Override
    public double getWantedZ() {
        return this.d;
    }

    @Override
    public boolean hasWanted() {
        return this.f;
    }

    @Override
    protected float rotlerp(float f, float f1, float f2) {
        float f3 = Mth.wrapDegrees(f1 - f);

        if (f3 > f2) {
            f3 = f2;
        }

        if (f3 < -f2) {
            f3 = -f2;
        }

        float f4 = f + f3;

        if (f4 < 0.0F)
            f4 += 360.0F;
        else if (f4 > 360.0F) {
            f4 -= 360.0F;
        }

        return f4;
    }

    @Override
    public void setWantedPosition(double d0, double d1, double d2, double d3) {
        this.b = d0;
        this.c = d1;
        this.d = d2;
        this.e = d3;
        this.f = true;
    }

    private boolean shouldSlimeJump() {
        if (!(this.a instanceof Slime)) {
            return false;
        }
        if (this.h-- <= 0) {
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
        this.a.zza = 0;
        if (this.f) {
            this.f = false;
            int i = Mth.floor(this.a.getBoundingBox().minY + 0.5D);
            double d0 = this.b - this.a.getX();
            double d1 = this.d - this.a.getZ();
            double d2 = this.c - i;
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;
            if (d3 < 2.500000277905201E-007D) {
                this.a.zza = 0.0F;
                return;
            }
            float f = (float) Math.toDegrees(Math.atan2(d1, d0)) - 90.0F;
            this.a.setYRot(rotlerp(this.a.getYRot(), f, 90.0F));
            NMS.setHeadYaw(a.getBukkitEntity(), this.a.getYRot());
            AttributeInstance speed = this.a.getAttribute(Attributes.MOVEMENT_SPEED);
            speed.setBaseValue(0.3D * this.e);
            float movement = (float) (this.e * speed.getValue());
            this.a.setSpeed(movement);
            this.a.zza = movement;
            if (shouldSlimeJump() || (d2 >= NMS.getStepHeight(a.getBukkitEntity()) && (d0 * d0 + d1 * d1) < 1.0D)) {
                this.h = cg();
                this.h /= 3;
                if (this.a instanceof EntityHumanNPC) {
                    ((EntityHumanNPC) this.a).getControllerJump().jump();
                } else {
                    ((Mob) this.a).getJumpControl().jump();
                }
            }
        }
    }
}