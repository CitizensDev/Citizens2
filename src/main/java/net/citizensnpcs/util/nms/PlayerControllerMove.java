package net.citizensnpcs.util.nms;

import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_8_R3.AttributeInstance;
import net.minecraft.server.v1_8_R3.GenericAttributes;
import net.minecraft.server.v1_8_R3.MathHelper;

import org.bukkit.craftbukkit.v1_8_R3.TrigMath;

public class PlayerControllerMove {
    protected EntityHumanNPC a;
    protected double b;
    protected double c;
    protected double d;
    protected double e;
    protected boolean f;

    public PlayerControllerMove(EntityHumanNPC entityinsentient) {
        this.a = entityinsentient;
        this.b = entityinsentient.locX;
        this.c = entityinsentient.locY;
        this.d = entityinsentient.locZ;
    }

    public boolean a() {
        return this.f;
    }

    public void a(double d0, double d1, double d2, double d3) {
        this.b = d0;
        this.c = d1;
        this.d = d2;
        this.e = d3;
        this.f = true;
    }

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

    public double b() {
        return this.e;
    }

    public void c() {
        this.a.ba = 0F;
        if (this.f) {
            this.f = false;
            int i = MathHelper.floor(this.a.getBoundingBox().b + 0.5D);
            double d0 = this.b - this.a.locX;
            double d1 = this.d - this.a.locZ;
            double d2 = this.c - i;
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;

            if (d3 >= 2.500000277905201E-007D) {
                float f = (float) (TrigMath.atan2(d1, d0) * 180.0D / 3.141592741012573D) - 90.0F;

                this.a.yaw = a(this.a.yaw, f, 30.0F);
                NMS.setHeadYaw(a, this.a.yaw);
                AttributeInstance speed = this.a.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
                speed.setValue(0.1D * this.e);
                float movement = (float) (this.e * speed.getValue()) * 10;
                this.a.ba = movement;
                if ((d2 > 0.0D) && (d0 * d0 + d1 * d1 < 1.0D))
                    this.a.getControllerJump().a();
            }
        }
    }

    public double d() {
        return this.b;
    }

    public double e() {
        return this.c;
    }

    public double f() {
        return this.d;
    }
}