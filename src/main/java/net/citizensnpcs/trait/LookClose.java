package net.citizensnpcs.trait;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.abstraction.WorldVector;
import net.citizensnpcs.api.abstraction.entity.Entity;
import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.api.attachment.Attachment;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;

public class LookClose extends Attachment implements Runnable, Toggleable {
    private boolean enabled = Setting.DEFAULT_LOOK_CLOSE.asBoolean();
    private Player lookingAt;
    private final NPC npc;

    public LookClose(NPC npc) {
        this.npc = npc;
    }

    private void faceEntity(Entity from, Entity at) {
        if (from.getWorld() != at.getWorld())
            return;
        WorldVector loc = from.getLocation();

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

        from.setRotation((float) yaw - 90, (float) pitch);
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        enabled = key.getBoolean("");
    }

    @Override
    public void run() {
        if (!enabled || npc.getAI().hasDestination())
            return;
        if (hasInvalidTarget()) {
            findNewTarget();
        }
        if (lookingAt != null) {
            faceEntity(npc.getEntity(), lookingAt);
        }
    }

    private void findNewTarget() {
        List<Entity> nearby = npc.getEntity().getNearbyEntities(2.5, 5, 2.5);
        Collections.sort(nearby, new Comparator<Entity>() {
            @Override
            public int compare(Entity o1, Entity o2) {
                double d1 = o1.getLocation().distanceSquared(npc.getEntity().getLocation());
                double d2 = o2.getLocation().distanceSquared(npc.getEntity().getLocation());
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
        if (lookingAt == null)
            return true;
        if (!lookingAt.isOnline() || lookingAt.getWorld() != npc.getEntity().getWorld()
                || lookingAt.getLocation().distanceSquared(npc.getEntity().getLocation()) > 5) {
            lookingAt = null;
            return true;
        }
        return false;
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", enabled);
    }

    @Override
    public boolean toggle() {
        return (enabled = !enabled);
    }

    @Override
    public String toString() {
        return "LookClose{" + enabled + "}";
    }
}