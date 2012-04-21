package net.citizensnpcs.trait;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.minecraft.server.EntityLiving;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class LookClose extends Trait implements Runnable, Toggleable {
    private boolean enabled = Setting.DEFAULT_LOOK_CLOSE.asBoolean();
    private Player lookingAt;
    private final NPC npc;

    public LookClose(NPC npc) {
        this.npc = npc;
    }

    private void faceEntity(Entity from, Entity at) {
        if (from.getWorld() != at.getWorld())
            return;
        Location loc = from.getLocation();

        double xDiff = at.getLocation().getX() - loc.getX();
        double yDiff = at.getLocation().getY() - loc.getY();
        double zDiff = at.getLocation().getZ() - loc.getZ();

        double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

        double yaw = (Math.acos(xDiff / distanceXZ) * 180 / Math.PI);
        double pitch = (Math.acos(yDiff / distanceY) * 180 / Math.PI) - 90;
        if (zDiff < 0.0) {
            yaw = yaw + (Math.abs(180 - yaw) * 2);
        }

        EntityLiving handle = ((CraftLivingEntity) from).getHandle();
        handle.yaw = (float) yaw - 90;
        handle.pitch = (float) pitch;
        handle.X = handle.yaw;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        enabled = key.getBoolean("enabled");
    }

    @Override
    public void run() {
        if (!enabled || npc.getAI().hasDestination())
            return;
        if (hasInvalidTarget()) {
            findNewTarget();
        } else {
            faceEntity(npc.getBukkitEntity(), lookingAt);
        }
    }

    private void findNewTarget() {
        List<Entity> nearby = npc.getBukkitEntity().getNearbyEntities(2.5, 5, 2.5);
        Collections.sort(nearby, new Comparator<Entity>() {
            @Override
            public int compare(Entity o1, Entity o2) {
                double d1 = o1.getLocation().distanceSquared(npc.getBukkitEntity().getLocation());
                double d2 = o2.getLocation().distanceSquared(npc.getBukkitEntity().getLocation());
                return Double.compare(d1, d2);
            }
        });
        for (Entity entity : nearby) {
            if (entity instanceof Player) {
                lookingAt = (Player) entity;
                return;
            }
        }
        lookingAt = null;
    }

    private boolean hasInvalidTarget() {
        return lookingAt == null || !lookingAt.isOnline()
                || lookingAt.getLocation().distanceSquared(npc.getBukkitEntity().getLocation()) > 5;
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("enabled", enabled);
    }

    @Override
    public boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    @Override
    public String toString() {
        return "LookClose{" + enabled + "}";
    }
}