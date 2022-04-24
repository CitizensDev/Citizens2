package net.citizensnpcs.trait;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

@TraitName("rotationtrait")
public class RotationTrait extends Trait {
    protected float maxPitchRotationPerTick = 10;
    protected float maxYawRotationPerTick = 40;
    protected boolean rotating;
    protected double tx;
    protected double ty;
    protected double tz;

    public RotationTrait() {
        super("rotationtrait");
    }

    private double getEyeY() {
        return NMS.getHeight(npc.getEntity());
    }

    protected float getTargetPitchDifference() {
        double dx = tx - getX();
        double dy = ty - (getY() + getEyeY());
        double dz = tz - getZ();
        double diag = Math.sqrt((float) (dx * dx + dz * dz));
        return (float) -Math.toDegrees(Math.atan2(dy, diag));
    }

    protected float getTargetYawDifference() {
        return (float) Math.toDegrees(Math.atan2(tz - getZ(), tx - getX())) - 90.0F;
    }

    private double getX() {
        return npc.getStoredLocation().getX();
    }

    private double getY() {
        return npc.getStoredLocation().getY();
    }

    private double getZ() {
        return npc.getStoredLocation().getZ();
    }

    public void rotateToFace(Entity target) {
        Location loc = target.getLocation();
        loc.setY(loc.getY() + NMS.getHeight(target));
        rotateToFace(loc);
    }

    public void rotateToFace(Location target) {
        this.tx = target.getX();
        this.ty = target.getY();
        this.tz = target.getZ();
        this.rotating = true;
    }

    protected float rotateTowards(float target, float current, float maxRotPerTick) {
        float diff = Util.clamp(current - target);
        return target + clamp(diff, -maxRotPerTick, maxRotPerTick);
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || npc.getNavigator().isNavigating()) {
            // npc.yHeadRot = Mth.rotateIfNecessary(npc.yHeadRot, npc.yBodyRot, 75);
            return;
        }
        if (true) {
            // npc.setXRot(0.0F);
        }
        if (this.rotating) {
            this.rotating = false;
            NMS.setHeadYaw(npc.getEntity(), Util.clamp(rotateTowards(NMS.getHeadYaw(npc.getEntity()),
                    getTargetYawDifference(), this.maxYawRotationPerTick)));
            float d = Util.clamp(NMS.getHeadYaw(npc.getEntity()) - 40);
            if (d > NMS.getYaw(npc.getEntity())) {
                NMS.setBodyYaw(npc.getEntity(), d);
            }
            if (d != NMS.getYaw(npc.getEntity())) {
                d = NMS.getHeadYaw(npc.getEntity()) + 40;
                while (d >= 180F) {
                    d -= 360F;
                }
                while (d < -180F) {
                    d += 360F;
                }
                if (d < NMS.getYaw(npc.getEntity())) {
                    NMS.setBodyYaw(npc.getEntity(), d);
                }
            }
            NMS.setPitch(npc.getEntity(), rotateTowards(npc.getStoredLocation().getPitch(), getTargetPitchDifference(),
                    this.maxPitchRotationPerTick));
        }
    }

    public static float clamp(float var0, float var1, float var2) {
        if (var0 < var1) {
            return var1;
        } else {
            return var0 > var2 ? var2 : var0;
        }
    }
}
