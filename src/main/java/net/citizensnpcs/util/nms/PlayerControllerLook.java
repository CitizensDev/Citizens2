package net.citizensnpcs.util.nms;

import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.minecraft.server.v1_7_R1.Entity;
import net.minecraft.server.v1_7_R1.EntityLiving;
import net.minecraft.server.v1_7_R1.MathHelper;

public class PlayerControllerLook {
    private final EntityHumanNPC a;
    private float b;
    private float c;
    private boolean d;
    private double e;
    private double f;
    private double g;

    public PlayerControllerLook(EntityHumanNPC entityinsentient) {
        this.a = entityinsentient;
    }

    public void a() {
        this.a.pitch = 0.0F;
        if (this.d) {
            this.d = false;
            double d0 = this.e - this.a.locX;
            double d1 = this.f - (this.a.locY + this.a.getHeadHeight());
            double d2 = this.g - this.a.locZ;
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            float f = (float) (Math.atan2(d2, d0) * 180.0D / 3.1415927410125732D) - 90.0F;
            float f1 = (float) (-(Math.atan2(d1, d3) * 180.0D / 3.1415927410125732D));

            this.a.pitch = this.a(this.a.pitch, f1, this.c);
            this.a.aP = this.a(this.a.aP, f, this.b);
        } else {
            this.a.aP = this.a(this.a.aP, this.a.aN, 10.0F);
        }

        float f2 = MathHelper.g(this.a.aP - this.a.aN);

        if (!this.a.isNavigating()) {
            if (f2 < -75.0F) {
                this.a.aP = this.a.aN - 75.0F;
            }

            if (f2 > 75.0F) {
                this.a.aP = this.a.aN + 75.0F;
            }
        }
    }

    public void a(double d0, double d1, double d2, float f, float f1) {
        this.e = d0;
        this.f = d1;
        this.g = d2;
        this.b = f;
        this.c = f1;
        this.d = true;
    }

    public void a(Entity entity, float f, float f1) {
        this.e = entity.locX;
        if (entity instanceof EntityLiving) {
            this.f = entity.locY + entity.getHeadHeight();
        } else {
            this.f = (entity.boundingBox.b + entity.boundingBox.e) / 2.0D;
        }

        this.g = entity.locZ;
        this.b = f;
        this.c = f1;
        this.d = true;
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
}
