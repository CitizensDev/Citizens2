package net.citizensnpcs.npc.ai;

import java.lang.reflect.Field;
import java.util.Random;

import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.util.Messaging;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.MathHelper;
import net.minecraft.server.PathEntity;
import net.minecraft.server.Vec3D;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MoveStrategy implements PathStrategy {
    private Float cachedSpeed;

    private final EntityLiving handle;
    private final PathEntity path;
    private final Random random = new Random();

    public MoveStrategy(CitizensNPC handle, Location destination) {
        this.handle = handle.getHandle();
        this.path = this.handle.world.a(this.handle, destination.getBlockX(), destination.getBlockY(),
                destination.getBlockZ(), 16F, true, false, false, true);
    }

    MoveStrategy(EntityLiving handle, PathEntity path) {
        this.handle = handle;
        this.path = path;
    }

    private Vec3D getVector() {
        Vec3D vec3d = path.a(handle);
        double lengthSq = (handle.width * 2.0F);
        lengthSq *= lengthSq;
        while (vec3d != null && vec3d.d(handle.locX, vec3d.b, handle.locZ) < lengthSq) {
            this.path.a(); // Increment path index.
            if (this.path.b()) { // finished.
                return null;
            }
            vec3d = this.path.a(handle);
        }
        return vec3d;
    }

    private float getYawDifference(double diffZ, double diffX) {
        float vectorYaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
        float diffYaw = (vectorYaw - handle.yaw) % 360;
        return Math.max(-30F, Math.min(30, diffYaw));
    }

    private void jump() {
        if (handle.onGround)
            handle.motY = JUMP_VELOCITY;
    }

    @Override
    public boolean update() {
        if (handle.dead)
            return true;
        Vec3D vector = getVector();
        if (vector == null)
            return true;
        int yHeight = MathHelper.floor(handle.boundingBox.b + 0.5D);
        boolean inWater = ((Player) handle.getBukkitEntity()).getRemainingAir() < 20;
        boolean onFire = handle.fireTicks > 0;
        double diffX = vector.a - handle.locX;
        double diffZ = vector.c - handle.locZ;

        handle.yaw += getYawDifference(diffZ, diffX);
        if (vector.b - yHeight > 0.0D)
            jump();
        if (cachedSpeed == null) {
            try {
                cachedSpeed = SPEED_FIELD.getFloat(handle);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Messaging.log(cachedSpeed);
        handle.e(cachedSpeed);
        // handle.walk();

        if (handle.positionChanged)
            jump();
        if (random.nextFloat() < 0.8F && (inWater || onFire))
            handle.motY += 0.04D;
        return false;
    }

    private static final double JUMP_VELOCITY = 0.49D;

    private static Field SPEED_FIELD;
    static {
        try {
            SPEED_FIELD = EntityLiving.class.getDeclaredField("bb");
            SPEED_FIELD.setAccessible(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}