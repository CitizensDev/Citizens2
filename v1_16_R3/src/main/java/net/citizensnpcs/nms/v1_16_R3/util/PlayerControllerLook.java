package net.citizensnpcs.nms.v1_16_R3.util;

import net.citizensnpcs.nms.v1_16_R3.entity.EntityHumanNPC;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.Vec3D;

public class PlayerControllerLook {
    private final EntityHumanNPC a;
    protected float b;
    protected float c;
    private final PlayerBodyControl control;
    protected boolean d;
    protected double e;
    protected double f;
    protected double g;

    public PlayerControllerLook(EntityHumanNPC entityinsentient) {
        this.a = entityinsentient;
        this.control = new PlayerBodyControl(this.a);
    }

    public void a() {
        if (!NMSImpl.isNavigationFinished(this.a.getNavigation()))
            return;
        if (this.b()) {
            // this.a.pitch = 0.0F;
        }
        if (this.d) {
            this.d = false;
            this.a.pitch = this.a(this.a.pitch, this.g(), this.c);
            this.a.aC = this.a(this.a.aC, this.h(), this.b);
            while (this.a.aC >= 180F) {
                this.a.aC -= 360F;
            }
            while (this.a.aC < -180F) {
                this.a.aC += 360F;
            }
            double d = this.a.aC - 40;
            while (d >= 180F) {
                d -= 360F;
            }
            while (d < -180F) {
                d += 360F;
            }
            if (d > this.a.yaw) {
                this.a.yaw = (float) d;
            }
            if (d != this.a.yaw) {
                d = this.a.aC + 40;
                while (d >= 180F) {
                    d -= 360F;
                }
                while (d < -180F) {
                    d += 360F;
                }
                if (d < this.a.yaw) {
                    this.a.yaw = (float) d;
                }
            }
            // this.a.yaw = this.a(this.a.aC, this.h(), this.b);
        } else {
            // this.a.yaw = MathHelper.b(this.a.yaw, this.a.aC, 40F);
            // this.a.aK = this.a(this.a.aC, this.a.aA, 10.0F);
        }
        if (!this.a.getNavigation().m()) {
            this.a.aC = MathHelper.b(this.a.aC, this.a.aA, 75);
        }
    }

    public void a(double var0, double var2, double var4) {
        this.a(var0, var2, var4, 10, 40);
    }

    public void a(double var0, double var2, double var4, float var6, float var7) {
        double d = Math.pow(this.e - var0, 2) + Math.pow(this.f - var2, 2) + Math.pow(this.g - var4, 2);
        if (d < 0.01) {
            // return;
        }
        this.e = var0;
        this.f = var2;
        this.g = var4;
        this.b = var6;
        this.c = var7;
        this.d = true;
    }

    public void a(Entity var0, float var1, float var2) {
        this.a(var0.locX(), b(var0), var0.locZ(), var1, var2);
    }

    protected float a(float var0, float var1, float var2) {
        float var3 = MathHelper.c(var0, var1);
        float var4 = MathHelper.a(var3, -var2, var2);
        return var0 + var4;
    }

    public void a(Vec3D var0) {
        this.a(var0.x, var0.y, var0.z);
    }

    protected boolean b() {
        return true;
    }

    public boolean c() {
        return this.d;
    }

    public double d() {
        return this.e;
    }

    public double e() {
        return this.f;
    }

    public double f() {
        return this.g;
    }

    protected float g() {
        double var0 = this.e - this.a.locX();
        double var2 = this.f - (this.a.locY() + this.a.getHeadHeight());
        double var4 = this.g - this.a.locZ();
        double var6 = MathHelper.sqrt(var0 * var0 + var4 * var4);
        return (float) (-(MathHelper.d(var2, var6) * 57.2957763671875D));
    }

    protected float h() {
        double var0 = this.e - this.a.locX();
        double var2 = this.g - this.a.locZ();
        return (float) (MathHelper.d(var2, var0) * 57.2957763671875D) - 90.0F;
    }

    private static double b(Entity var0) {
        return var0 instanceof EntityLiving ? var0.locY() + var0.getHeadHeight()
                : (var0.getBoundingBox().minY + var0.getBoundingBox().maxY) / 2.0D;
    }
}