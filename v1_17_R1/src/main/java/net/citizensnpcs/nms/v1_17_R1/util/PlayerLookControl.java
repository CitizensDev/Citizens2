package net.citizensnpcs.nms.v1_17_R1.util;

import net.citizensnpcs.nms.v1_17_R1.entity.EntityHumanNPC;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class PlayerLookControl {
    private final EntityHumanNPC a;
    protected float b;
    protected float c;
    private final PlayerBodyControl control;
    protected boolean d;
    protected double e;
    protected double f;
    protected double g;

    public PlayerLookControl(EntityHumanNPC entityinsentient) {
        this.a = entityinsentient;
        this.control = new PlayerBodyControl(this.a);
    }

    public void a() {
        if (!NMSImpl.isNavigationFinished(this.a.getNavigation()))
            return;
        if (this.b()) {
            // this.a.setXRot(0.0F);
        }
        if (this.d) {
            this.d = false;
            this.a.setXRot(this.a(this.a.getXRot(), this.g(), this.c));
            this.a.yHeadRot = this.a(this.a.yHeadRot, this.h(), this.b);
            while (this.a.yHeadRot >= 180F) {
                this.a.yHeadRot -= 360F;
            }
            while (this.a.yHeadRot < -180F) {
                this.a.yHeadRot += 360F;
            }
            double d = this.a.yHeadRot - 40;
            while (d >= 180F) {
                d -= 360F;
            }
            while (d < -180F) {
                d += 360F;
            }
            if (d > this.a.getYRot()) {
                this.a.setYRot((float) d);
            }
            if (d != this.a.getYRot()) {
                d = this.a.yHeadRot + 40;
                while (d >= 180F) {
                    d -= 360F;
                }
                while (d < -180F) {
                    d += 360F;
                }
                if (d < this.a.yHeadRot) {
                    this.a.yHeadRot = (float) d;
                }
            }
            // this.a.setYRot(this.a(this.a.yHeadRot, this.h(), this.b));
        } else {
            // this.a.setYRot(MathHelper.b(this.a.getYRot(), this.a.yHeadRot, 40F));
            // this.a.aK = this.a(this.a.yHeadRot, this.a.aA, 10.0F);
        }
        if (!this.a.getNavigation().isDone()) { // TODO: use Citizens AI?
            this.a.yHeadRot = Mth.rotateIfNecessary(this.a.yHeadRot, this.a.yBodyRot, 75);
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
        this.a(var0.getX(), b(var0), var0.getZ(), var1, var2);
    }

    protected float a(float var0, float var1, float var2) {
        float var3 = Mth.degreesDifference(var0, var1);
        float var4 = Mth.clamp(var3, -var2, var2);
        return var0 + var4;
    }

    public void a(Vec3 var0) {
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
        double var0 = this.e - this.a.getX();
        double var2 = this.f - (this.a.getY() + this.a.getEyeY());
        double var4 = this.g - this.a.getZ();
        double var6 = Mth.sqrt((float) (var0 * var0 + var4 * var4));
        return (float) (-(Mth.atan2(var2, var6) * 57.2957763671875D));
    }

    protected float h() {
        double var0 = this.e - this.a.getX();
        double var2 = this.g - this.a.getZ();
        return (float) (Mth.atan2(var2, var0) * 57.2957763671875D) - 90.0F;
    }

    private static double b(Entity var0) {
        return var0 instanceof LivingEntity ? var0.getY() + var0.getEyeY()
                : (var0.getBoundingBox().minY + var0.getBoundingBox().maxY) / 2.0D;
    }
}