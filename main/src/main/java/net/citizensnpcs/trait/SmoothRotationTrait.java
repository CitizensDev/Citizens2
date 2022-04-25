package net.citizensnpcs.trait;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.Persistable;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

@TraitName("smoothrotationtrait")
public class SmoothRotationTrait extends Trait {
    @Persist
    private Float defaultPitch;
    @Persist(reify = true)
    private final RotationParams globalParameters = new RotationParams();
    private final SmoothRotationSession globalSession = new SmoothRotationSession(globalParameters);

    public SmoothRotationTrait() {
        super("smoothrotationtrait");
    }

    private double getEyeY() {
        return NMS.getHeight(npc.getEntity());
    }

    public RotationParams getGlobalParameters() {
        return globalParameters;
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
        this.globalSession.setTarget(target);
    }

    public void rotateToHave(float yaw, float pitch) {
        Vector vector = new Vector(Math.cos(yaw) * Math.cos(pitch), Math.sin(pitch), Math.sin(yaw) * Math.cos(pitch))
                .normalize();
        rotateToFace(npc.getEntity().getLocation().clone().add(vector));
    }

    @Override
    public void run() {
        if (!npc.isSpawned() || npc.getNavigator().isNavigating()) {
            // npc.yHeadRot = Mth.rotateIfNecessary(npc.yHeadRot, npc.yBodyRot, 75);
            return;
        }
        if (!globalSession.hasTarget()) {
            return;
        }
        EntityRotation rot = new EntityRotation(npc.getEntity());
        globalSession.run(rot);
        if (!globalSession.hasTarget()) {
            rot.bodyYaw = rot.headYaw;
        }
        rot.apply(npc.getEntity());
    }

    public void setDefaultPitch(float pitch) {
        defaultPitch = pitch;
    }

    public static class EntityRotation {
        public float bodyYaw, headYaw, pitch;

        public EntityRotation(Entity entity) {
            this.bodyYaw = NMS.getYaw(entity);
            this.headYaw = NMS.getHeadYaw(entity);
            this.pitch = entity.getLocation().getPitch();
        }

        public void apply(Entity entity) {
            NMS.setBodyYaw(entity, bodyYaw);
            NMS.setHeadYaw(entity, headYaw);
            NMS.setPitch(entity, pitch);
        }
    }

    public static class RotationParams implements Persistable {
        private boolean headOnly = false;
        private boolean immediate = false;
        private float maxPitchPerTick = 10;
        private float maxYawPerTick = 40;
        private final float[] pitchRange = { 0, 0 };
        private final float[] yawRange = { 0, 0 };

        public RotationParams headOnly(boolean headOnly) {
            this.headOnly = headOnly;
            return this;
        }

        public RotationParams immediate(boolean immediate) {
            this.immediate = immediate;
            return this;
        }

        @Override
        public void load(DataKey key) {
            if (key.keyExists("headOnly")) {
                headOnly = key.getBoolean("headOnly");
            }
            if (key.keyExists("immediate")) {
                immediate = key.getBoolean("immediate");
            }
            if (key.keyExists("maxPitchPerTick")) {
                maxPitchPerTick = (float) key.getDouble("maxPitchPerTick");
            }
            if (key.keyExists("maxYawPerTick")) {
                maxYawPerTick = (float) key.getDouble("maxYawPerTick");
            }
        }

        public RotationParams maxPitchPerTick(float val) {
            this.maxPitchPerTick = val;
            return this;
        }

        public RotationParams maxYawPerTick(float val) {
            this.maxYawPerTick = val;
            return this;
        }

        public float rotateHeadYawTowards(int t, float yaw, float targetYaw) {
            return rotateTowards(yaw, targetYaw, maxYawPerTick);
        }

        public float rotatePitchTowards(int t, float pitch, float targetPitch) {
            return rotateTowards(pitch, targetPitch, maxPitchPerTick);
        }/*
         *  public Vector3 SuperSmoothVector3Lerp( Vector3 pastPosition, Vector3 pastTargetPosition, Vector3 targetPosition, float time, float speed ){
         Vector3 f = pastPosition - pastTargetPosition + (targetPosition - pastTargetPosition) / (speed * time);
         return targetPosition - (targetPosition - pastTargetPosition) / (speed*time) + f * Mathf.Exp(-speed*time);
         }
         */

        private float rotateTowards(float target, float current, float maxRotPerTick) {
            float diff = Util.clamp(current - target);
            return target + clamp(diff, -maxRotPerTick, maxRotPerTick);
        }

        @Override
        public void save(DataKey key) {
            if (headOnly) {
                key.setBoolean("headOnly", headOnly);
            }
            if (immediate) {
                key.setBoolean("immediate", immediate);
            }
            if (maxPitchPerTick != 10) {
                key.setDouble("maxPitchPerTick", maxPitchPerTick);
            }
            if (maxYawPerTick != 40) {
                key.setDouble("maxYawPerTick", maxYawPerTick);
            }
        }
    }

    public class SmoothRotationSession {
        private final RotationParams params;
        private int t;
        private double tx, ty, tz;

        public SmoothRotationSession(RotationParams params) {
            this.params = params;
        }

        private float getTargetPitch() {
            double dx = tx - getX();
            double dy = ty - (getY() + getEyeY());
            double dz = tz - getZ();
            double diag = Math.sqrt((float) (dx * dx + dz * dz));
            return (float) -Math.toDegrees(Math.atan2(dy, diag));
        }

        private float getTargetYaw() {
            return (float) Math.toDegrees(Math.atan2(tz - getZ(), tx - getX())) - 90.0F;
        }

        public boolean hasTarget() {
            return t >= 0;
        }

        public void run(EntityRotation rot) {
            if (!hasTarget())
                return;
            rot.headYaw = params.immediate ? getTargetYaw()
                    : Util.clamp(params.rotateHeadYawTowards(t, rot.headYaw, getTargetYaw()));
            if (!params.headOnly) {
                float d = Util.clamp(rot.headYaw - 35);
                if (d > rot.bodyYaw) {
                    rot.bodyYaw = d;
                }
                if (d != rot.bodyYaw) {
                    d = Util.clamp(rot.headYaw + 35);
                    if (d < rot.bodyYaw) {
                        rot.bodyYaw = d;
                    }
                }
            }
            rot.pitch = params.immediate ? getTargetPitch() : params.rotatePitchTowards(t, rot.pitch, getTargetPitch());
            t++;
            if (Math.abs(rot.pitch - getTargetPitch()) + Math.abs(rot.headYaw - getTargetYaw()) < 0.1) {
                t = -1;
            }
        }

        public void setTarget(Location target) {
            tx = target.getX();
            ty = target.getY();
            tz = target.getZ();
            t = 0;
        }
    }

    private static float clamp(float orig, float min, float max) {
        return Math.max(min, Math.min(max, orig));
    }
}
