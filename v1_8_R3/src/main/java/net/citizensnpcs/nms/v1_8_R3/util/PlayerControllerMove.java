package net.citizensnpcs.nms.v1_8_R3.util;

import java.util.Random;

import net.citizensnpcs.nms.v1_8_R3.entity.EntityHumanNPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_8_R3.AttributeInstance;
import net.minecraft.server.v1_8_R3.ControllerMove;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntitySlime;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.MathHelper;

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
    public boolean a() {
        return this.f;
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
        if (f4 < 0.0F) {
            f4 += 360.0F;
        } else if (f4 > 360.0F) {
            f4 -= 360.0F;
        }
        return f4;
    }

    @Override
    public double b() {
        return this.e;
    }

    @Override
    public void c() {
        this.a.ba = 0F;
        if (!this.f)
            return;
        this.f = false;
        double dX = this.b - this.a.locX;
        double dZ = this.d - this.a.locZ;
        double dY = this.c - this.a.locY;
        double dXZ = Math.sqrt(dX * dX + dZ * dZ);
        if (Math.abs(dY) < 1.0 && dXZ < 0.025)
            return;
        float f = (float) Math.toDegrees(Math.atan2(dZ, dX)) - 90.0F;
        this.a.yaw = a(this.a.yaw, f, 90.0F);
        NMS.setHeadYaw(a.getBukkitEntity(), this.a.yaw);
        AttributeInstance speed = this.a.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
        float movement = (float) (this.e * speed.getValue());
        this.a.k(movement);
        this.a.ba = movement;
        if (a instanceof EntitySlime && h-- <= 0) {
            this.h = new Random().nextInt(20) + 10;
            this.h /= 3;
            ((EntityInsentient) this.a).getControllerJump().a();
        } else if (dY >= NMS.getStepHeight(a.getBukkitEntity()) && dXZ < 0.4D) {
            if (this.a instanceof EntityHumanNPC) {
                ((EntityHumanNPC) this.a).getControllerJump().a();
            } else {
                ((EntityInsentient) this.a).getControllerJump().a();
            }
        }
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
}