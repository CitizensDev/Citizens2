package net.citizensnpcs.util.nms;

import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_6_R3.AttributeInstance;
import net.minecraft.server.v1_6_R3.GenericAttributes;
import net.minecraft.server.v1_6_R3.MathHelper;

public class PlayerControllerMove {
    private final EntityHumanNPC a;
    private double b;
    private double c;
    private double d;
    private double e;
    private boolean f;

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

    private float a(float f, float f1, float f2) {
        float f3 = MathHelper.g(f1 - f);

        if (f3 > f2) {
            f3 = f2;
        }

        if (f3 < -f2) {
            f3 = -f2;
        }

        return f + f3;
    }

    public double b() {
        return this.e;
    }

    public void c() {
        this.a.bf = 0F;
        if (this.f) {
            this.f = false;
            int i = MathHelper.floor(this.a.boundingBox.b + 0.5D);
            double d0 = this.b - this.a.locX;
            double d1 = this.d - this.a.locZ;
            double d2 = this.c - i;
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;

            if (d3 >= 2.500000277905201E-7D) {
                float f = (float) (Math.atan2(d1, d0) * 180.0D / 3.1415927410125732D) - 90.0F;

                this.a.yaw = this.a(this.a.yaw, f, 30.0F);
                NMS.setHeadYaw(a, this.a.yaw);
                AttributeInstance speed = this.a.getAttributeInstance(GenericAttributes.d);
                speed.setValue(0.1D * this.e);
                float movement = (float) (this.e * speed.getValue()) * 10;
                this.a.i(movement);
                this.a.bf = movement;
                if (d2 > 0.0D && d0 * d0 + d1 * d1 < 1.0D) {
                    this.a.getControllerJump().a();
                }
            }
        }
    }
}
