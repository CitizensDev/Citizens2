package net.citizensnpcs.npc.ai;

import java.util.Random;

import net.citizensnpcs.npc.CitizensNPC;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.MathHelper;
import net.minecraft.server.PathEntity;
import net.minecraft.server.Vec3D;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MoveStrategy implements PathStrategy {
    private static final double JUMP_VELOCITY = 0.49D;

    private final EntityLiving handle;
    private final PathEntity path;
    private final Random random = new Random();

    public MoveStrategy(CitizensNPC handle, Location destination) {
        this.handle = handle.getHandle();
        this.path = this.handle.world.a(this.handle, destination.getBlockX(), destination.getBlockY(),
                destination.getBlockZ(), 16F);
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
            if (this.path.b())// finished.
                return null;
            else
                vec3d = this.path.a(handle);
        }
        return vec3d;
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
        handle.d(handle.ar());
        handle.d();
        // handle.walk();

        if (handle.positionChanged)
            jump();
        if (random.nextFloat() < 0.8F && (inWater || onFire))
            handle.motY += 0.04D;
        return false;
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
}