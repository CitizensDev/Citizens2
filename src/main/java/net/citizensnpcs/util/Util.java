package net.citizensnpcs.util;

import java.util.Random;

import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class Util {
    // Static class for small (emphasis small) utility methods
    private Util() {
    }

    public static void assumePose(LivingEntity entity, float yaw, float pitch) {
        NMS.look(entity, yaw, pitch);
    }

    public static void callCollisionEvent(NPC npc, Entity entity) {
        if (NPCCollisionEvent.getHandlerList().getRegisteredListeners().length > 0)
            Bukkit.getPluginManager().callEvent(new NPCCollisionEvent(npc, entity));
    }

    public static NPCPushEvent callPushEvent(NPC npc, Vector vector) {
        NPCPushEvent event = new NPCPushEvent(npc, vector);
        event.setCancelled(npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void faceEntity(LivingEntity from, LivingEntity at) {
        if (from.getWorld() != at.getWorld())
            return;
        faceLocation(from, at.getLocation(AT_LOCATION));
    }

    public static void faceLocation(LivingEntity from, Location to) {
        if (from.getWorld() != to.getWorld())
            return;
        Location fromLocation = from.getLocation(FROM_LOCATION);
        double xDiff, yDiff, zDiff;
        xDiff = to.getX() - fromLocation.getX();
        yDiff = to.getY() - fromLocation.getY();
        zDiff = to.getZ() - fromLocation.getZ();

        double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

        double yaw = Math.toDegrees(Math.acos(xDiff / distanceXZ));
        double pitch = Math.toDegrees(Math.acos(yDiff / distanceY)) - 90;
        if (zDiff < 0.0)
            yaw += Math.abs(180 - yaw) * 2;

        NMS.look(from, (float) yaw - 90, (float) pitch);
    }

    public static Random getFastRandom() {
        return new XORShiftRNG();
    }

    public static String getMinecraftVersion() {
        String raw = Bukkit.getVersion();
        int start = raw.indexOf("MC:");
        if (start == -1)
            return raw;
        start += 4;
        int end = raw.indexOf(')', start);
        return raw.substring(start, end);
    }

    public static boolean isLoaded(Location location) {
        if (location.getWorld() == null)
            return false;
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return location.getWorld().isChunkLoaded(chunkX, chunkZ);
    }

    public static String listValuesPretty(Enum<?>[] values) {
        return Joiner.on(", ").join(values).toLowerCase().replace('_', ' ');
    }

    public static boolean locationWithinRange(Location current, Location target, double range) {
        if (current == null || target == null)
            return false;
        if (current.getWorld() != target.getWorld())
            return false;
        return current.distanceSquared(target) < Math.pow(range, 2);
    }

    public static EntityType matchEntityType(String toMatch) {
        EntityType type = EntityType.fromName(toMatch);
        if (type != null)
            return type;
        return matchEnum(EntityType.values(), toMatch);
    }

    public static <T extends Enum<?>> T matchEnum(T[] values, String toMatch) {
        toMatch = toMatch.toLowerCase().replace('-', '_').replace(' ', '_');
        for (T check : values) {
            if (toMatch.equals(check.name().toLowerCase())) {
                return check; // check for an exact match first
            }
        }
        for (T check : values) {
            String name = check.name().toLowerCase();
            if (name.replace("_", "").equals(toMatch) || name.matches(toMatch) || name.startsWith(toMatch)) {
                return check;
            }
        }
        return null;
    }

    public static boolean matchesItemInHand(Player player, String setting) {
        String parts = setting;
        if (parts.contains("*"))
            return true;
        for (String part : Splitter.on(',').split(parts)) {
            if (Material.matchMaterial(part) == player.getItemInHand().getType()) {
                return true;
            }
        }
        return false;
    }

    public static String prettyEnum(Enum<?> e) {
        return e.name().toLowerCase().replace('_', ' ');
    }

    private static final Location AT_LOCATION = new Location(null, 0, 0, 0);
    private static final Location FROM_LOCATION = new Location(null, 0, 0, 0);
}
