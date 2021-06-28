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
    private int h;
    protected boolean moving;
    protected double speed;
    protected double tx;
    protected double ty;
    protected double tz;

    public PlayerMoveControl(LivingEntity entityinsentient) {
        super(entityinsentient instanceof Mob ? (Mob) entityinsentient
                : new Slime(EntityType.SLIME, entityinsentient.level));
        this.a = entityinsentient;
        this.tx = entityinsentient.getX();
        this.ty = entityinsentient.getY();
        this.tz = entityinsentient.getZ();
    }

    protected int cg() {
        return new Random().nextInt(20) + 10;
    }

    @Override
    public double getSpeedModifier() {
        return this.speed;
    }

    @Override
    public double getWantedX() {
        return this.tx;
    }

    @Override
    public double getWantedY() {
        return this.ty;
    }

    @Override
    public double getWantedZ() {
        return this.tz;
    }

    @Override
    public boolean hasWanted() {
        return this.moving;
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
        this.tx = d0;
        this.ty = d1;
        this.tz = d2;
        this.speed = d3;
        this.moving = true;
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
        if (this.moving) {
            this.moving = false;
            double dX = this.tx - this.a.getX();
            double dZ = this.tz - this.a.getZ();
            double dY = this.ty - this.a.getY();
            double dXZ = dY * dY + dZ * dZ;
            if (dY * dY < 1.0 && dXZ < 2.500000277905201E-007D) {
                this.a.zza = 0.0F;
                return;
            }
            float f = (float) Math.toDegrees(Mth.atan2(dZ, dX)) - 90.0F;
            this.a.setYRot(rotlerp(this.a.getYRot(), f, 90.0F));
            NMS.setHeadYaw(a.getBukkitEntity(), this.a.getYRot());
            AttributeInstance speed = this.a.getAttribute(Attributes.MOVEMENT_SPEED);
            speed.setBaseValue(0.3D * this.speed);
            float movement = (float) (this.speed * speed.getValue());
            this.a.setSpeed(movement);
            this.a.zza = movement;
            if (shouldSlimeJump() || (dY >= NMS.getStepHeight(a.getBukkitEntity()) && dXZ < 1.0D)) {
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