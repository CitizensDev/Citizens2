package net.citizensnpcs.nms.v1_16_R3.util;

import net.citizensnpcs.nms.v1_16_R3.entity.EntityHumanNPC;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.MathHelper;

public class PlayerBodyControl {
    private final EntityHumanNPC a;
    private int b;
    private float c;

    public PlayerBodyControl(EntityHumanNPC var0) {
        this.a = var0;
    }

    public void a() {
        if (f()) {
            this.a.aA = this.a.yaw;
            c();
            this.c = this.a.aC;
            this.b = 0;
            return;
        }
        if (e())
            if (Math.abs(this.a.aC - this.c) > 15.0F) {
                System.out.println("BIG DX");
                this.b = 0;
                this.c = this.a.aC;
                b();
            } else {
                this.b++;
                if (this.b > 10)
                    d();
            }
    }

    private void b() {
        this.a.aA = MathHelper.b(this.a.aA, this.a.aC, 10);
        this.a.yaw = this.a.aA;
    }

    private void c() {
        this.a.aC = MathHelper.b(this.a.aC, this.a.aA, 10);
    }

    private void d() {
        int var0 = this.b - 10;
        float var1 = MathHelper.a(var0 / 10.0F, 0.0F, 1.0F);
        float var2 = 40 * (1.0F - var1);
        this.a.aA = MathHelper.b(this.a.aA, this.a.aC, var2);
    }

    private boolean e() {
        return (this.a.getPassengers().isEmpty() || !(this.a.getPassengers().get(0) instanceof EntityInsentient));
    }

    private boolean f() {
        double var0 = this.a.locX() - this.a.lastX;
        double var2 = this.a.locZ() - this.a.lastZ;
        return (var0 * var0 + var2 * var2 > 2.500000277905201E-7D);
    }
}