package net.citizensnpcs.nms.v1_12_R1.util;

import java.util.Random;

import net.citizensnpcs.nms.v1_12_R1.entity.EntityHumanNPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_12_R1.AttributeInstance;
import net.minecraft.server.v1_12_R1.ControllerMove;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EntitySlime;
import net.minecraft.server.v1_12_R1.GenericAttributes;
import net.minecraft.server.v1_12_R1.MathHelper;

public class PlayerControllerMove extends ControllerMove {
    protected EntityLiving a;
    protected double b;
    protected double c;
    protected double d;
    protected double e;
    protected boolean f;
    private int h;

    public PlayerControllerMove(EntityLiving entityinsentient) {
        super(entityinsentient instanceof EntityInsentient ? (EntityInsentient) entityinsentient
                : new EntitySlime(entityinsentient.world));
        this.a = entityinsentient;
        this.b = entityinsentient.locX;
        this.c = entityinsentient.locY;
        this.d = entityinsentient.locZ;
    }

    @Override
    public void a() {
        this.a.bg = 0F;
        if (this.f) {
            this.f = false;
            int i = MathHelper.floor(this.a.getBoundingBox().b + 0.5D);
            double d0 = this.b - this.a.locX;
            double d1 = this.d - this.a.locZ;
            double d2 = this.c - i;
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;
            if (d3 < 2.500000277905201E-007D) {
                this.a.bf = (0.0F);
                return;
            }
            float f = (float) Math.toDegrees(Math.atan2(d1, d0)) - 90.0F;
            this.a.yaw = a(this.a.yaw, f, 90.0F);
            NMS.setHeadYaw(a.getBukkitEntity(), this.a.yaw);
            AttributeInstance speed = this.a.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
            if (!(this.a instanceof EntitySlime)) {
                speed.setValue(0.1D * this.e);
            }
            float movement = (float) (this.e * speed.getValue()) * 10;
            this.a.k(movement);
            this.a.bg = movement;
            if (shouldSlimeJump() || ((d2 > 0.0D) && (d0 * d0 + d1 * d1 < 1.0D))) {
                this.h = cg();
                this.h /= 3;
                if (this.a instanceof EntityHumanNPC) {
                    ((EntityHumanNPC) this.a).getControllerJump().a();
                } else {
                    ((EntityInsentient) this.a).getControllerJump().a();
                }
            }
        }
    }

    @Override
    public void a(double d0, double d1, double d2, double d3) {
        this.b = d0;
        this.c = d1;
        this.d = d2;
        this.e = d3;
        this.f = true;
    }

    @Override
    protected float a(float f, float f1, float f2) {
        float f3 = MathHelper.g(f1 - f);

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
    public boolean b() {
        return this.f;
    }

    @Override
    public double c() {
        return this.e;
    }

    protected int cg() {
        return new Random().nextInt(20) + 10;
    }

    @Override
    public double d() {
        return this.b;
    }

    @Override
    public double e() {
        return this.c;
    }

    @Override
    public double f() {
        return this.d;
    }

    private boolean shouldSlimeJump() {
        if (!(this.a instanceof EntitySlime)) {
            return false;
        }
        if (this.h-- <= 0) {
            return true;
        }
        return false;
    }
}