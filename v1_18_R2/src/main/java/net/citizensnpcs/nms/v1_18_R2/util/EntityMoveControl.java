package net.citizensnpcs.nms.v1_18_R2.util;

import java.util.Random;

import net.citizensnpcs.util.NMS;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Slime;

public class EntityMoveControl extends MoveControl {
    protected LivingEntity entity;
    private int jumpTicks;
    protected boolean moving;
    protected double speedMod;
    protected double tx;
    protected double ty;
    protected double tz;

    public EntityMoveControl(LivingEntity entityinsentient) {
        super(entityinsentient instanceof Mob ? (Mob) entityinsentient
                : new Slime(EntityType.SLIME, entityinsentient.level));
        this.entity = entityinsentient;
        this.tx = entityinsentient.getX();
        this.ty = entityinsentient.getY();
        this.tz = entityinsentient.getZ();
    }

    @Override
    public double getSpeedModifier() {
        return this.speedMod;
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

    protected int jumpTicks() {
        return new Random().nextInt(20) + 10;
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
        if (f4 < 0.0F) {
            f4 += 360.0F;
        } else if (f4 > 360.0F) {
            f4 -= 360.0F;
        }
        return f4;
    }

    @Override
    public void setWantedPosition(double d0, double d1, double d2, double d3) {
        this.tx = d0;
        this.ty = d1;
        this.tz = d2;
        this.speedMod = d3;
        this.moving = true;
    }

    @Override
    public void tick() {
        this.entity.zza = 0;
        if (this.moving) {
            this.moving = false;
            double dX = this.tx - this.entity.getX();
            double dZ = this.tz - this.entity.getZ();
            double dY = this.ty - this.entity.getY();
            double dXZ = Math.sqrt(dX * dX + dZ * dZ);
            if (Math.abs(dY) < 1.0 && dXZ < 0.01)
                // this.entity.zza = 0.0F;
                return;
            if (dXZ > 0.4) {
                float f = (float) Math.toDegrees(Mth.atan2(dZ, dX)) - 90.0F;
                this.entity.setYRot(rotlerp(this.entity.getYRot(), f, 90.0F));
                NMS.setHeadYaw(entity.getBukkitEntity(), this.entity.getYRot());
            }
            float movement = (float) (this.speedMod * this.entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
            this.entity.setSpeed(movement);
            this.entity.zza = movement;
            if (entity instanceof Slime && jumpTicks-- <= 0) {
                this.jumpTicks = new Random().nextInt(20) + 10;
                if (((Slime) entity).isAggressive()) {
                    this.jumpTicks /= 3;
                }
                ((Slime) entity).getJumpControl().jump();
            } else if (dY >= NMS.getStepHeight(entity.getBukkitEntity()) && dXZ < 0.4D) {
                if (entity instanceof Mob) {
                    ((Mob) entity).getJumpControl().jump();
                }
                entity.setJumping(true);
            }
        }
    }
}