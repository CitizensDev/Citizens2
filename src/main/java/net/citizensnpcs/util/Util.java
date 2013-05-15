package net.citizensnpcs.util;

import java.util.Random;

import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public class Util {
    // Static class for small (emphasis small) utility methods
    private Util() {
    }

    private static final Location AT_LOCATION = new Location(null, 0, 0, 0);
    private static final Location FROM_LOCATION = new Location(null, 0, 0, 0);

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
        Location atLocation = at.getLocation(AT_LOCATION);
        Location fromLocation = from.getLocation(FROM_LOCATION);
        double xDiff, yDiff, zDiff;
        xDiff = atLocation.getX() - fromLocation.getX();
        yDiff = atLocation.getY() - fromLocation.getY();
        zDiff = atLocation.getZ() - fromLocation.getZ();

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

    public static EntityType matchEntityType(String toMatch) {
        EntityType type = EntityType.fromName(toMatch);
        if (type != null)
            return type;
        return matchEnum(EntityType.values(), toMatch);
    }

    public static <T extends Enum<?>> T matchEnum(T[] values, String toMatch) {
        T type = null;
        for (T check : values) {
            String name = check.name();
            if (name.matches(toMatch) || name.equalsIgnoreCase(toMatch)
                    || name.replace("_", "").equalsIgnoreCase(toMatch)
                    || name.replace('_', '-').equalsIgnoreCase(toMatch)
                    || name.replace('_', ' ').equalsIgnoreCase(toMatch) || name.startsWith(toMatch)) {
                type = check;
                break;
            }
        }
        return type;
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

    public static Location parseLocation(Location currentLocation, String flag) throws CommandException {
        String[] parts = Iterables.toArray(Splitter.on(':').split(flag), String.class);
        if (parts.length > 0) {
            String worldName = currentLocation != null ? currentLocation.getWorld().getName() : "";
            int x = 0, y = 0, z = 0;
            float yaw = 0F, pitch = 0F;
            switch (parts.length) {
                case 6:
                    pitch = Float.parseFloat(parts[5]);
                case 5:
                    yaw = Float.parseFloat(parts[4]);
                case 4:
                    worldName = parts[3];
                case 3:
                    x = Integer.parseInt(parts[0]);
                    y = Integer.parseInt(parts[1]);
                    z = Integer.parseInt(parts[2]);
                    break;
                default:
                    throw new CommandException(Messages.INVALID_SPAWN_LOCATION);
            }
            World world = Bukkit.getWorld(worldName);
            if (world == null)
                throw new CommandException(Messages.INVALID_SPAWN_LOCATION);
            return new Location(world, x, y, z, yaw, pitch);
        } else {
            Player search = Bukkit.getPlayerExact(flag);
            if (search == null)
                throw new CommandException(Messages.PLAYER_NOT_FOUND_FOR_SPAWN);
            return search.getLocation();
        }
    }
}
