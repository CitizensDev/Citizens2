package net.citizensnpcs.nms.v1_18_R1.util;

import net.citizensnpcs.nms.v1_18_R1.entity.EntityHumanNPC;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

public class PlayerBodyControl {
    private int headStableTime;
    private float lastStableYHeadRot;
    private final EntityHumanNPC mob;

    public PlayerBodyControl(EntityHumanNPC var0) {
        this.mob = var0;
    }

    public void a() {
        if (isMoving()) {
            this.mob.yBodyRot = this.mob.getYRot();
            rotateHeadIfNecessary();
            this.lastStableYHeadRot = this.mob.yHeadRot;
            this.headStableTime = 0;
            return;
        }
        if (e())
            if (Math.abs(this.mob.yHeadRot - this.lastStableYHeadRot) > 15.0F) {
                System.out.println("BIG DX");
                this.lastStableYHeadRot = 0;
                this.lastStableYHeadRot = this.mob.yHeadRot;
                rotateBodyIfNecessary();
            } else {
                if (++this.lastStableYHeadRot > 10) {
                    rotateHeadTowardsFront();
                }
            }
    }

    private boolean e() {
        return !(this.mob.getFirstPassenger() instanceof Mob);
    }

    private boolean isMoving() {
        double var0 = this.mob.getX() - this.mob.xo;
        double var2 = this.mob.getZ() - this.mob.zo;
        return (var0 * var0 + var2 * var2 > 2.500000277905201E-7D);
    }

    private void rotateBodyIfNecessary() {
        this.mob.yBodyRot = Mth.rotateIfNecessary(this.mob.yBodyRot, this.mob.yHeadRot, 40);
    }

    private void rotateHeadIfNecessary() {
        this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, 40);
    }

    private void rotateHeadTowardsFront() {
        int var0 = this.headStableTime - 10;
        float var1 = Mth.clamp(var0 / 10.0F, 0.0F, 1.0F);
        float var2 = 40 * (1.0F - var1);
        this.mob.yBodyRot = Mth.rotateIfNecessary(this.mob.yBodyRot, this.mob.yHeadRot, var2);
    }
}