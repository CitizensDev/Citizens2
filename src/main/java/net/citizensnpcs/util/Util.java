package net.citizensnpcs.util;

import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.Packet;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.google.common.base.Splitter;

public class Util {

    // Static class for small (emphasis small) utility methods
    private Util() {
    }

    public static void assumePose(org.bukkit.entity.Entity entity, Pose pose) {
        EntityLiving handle = ((CraftLivingEntity) entity).getHandle();
        NMS.look(handle, pose.getYaw(), pose.getPitch());
    }

    public static void callCollisionEvent(NPC npc, net.minecraft.server.Entity entity) {
        if (NPCCollisionEvent.getHandlerList().getRegisteredListeners().length > 0)
            Bukkit.getPluginManager().callEvent(new NPCCollisionEvent(npc, entity.getBukkitEntity()));
    }

    public static NPCPushEvent callPushEvent(NPC npc, Vector vector) {
        NPCPushEvent event = new NPCPushEvent(npc, vector);
        event.setCancelled(npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void faceEntity(Entity from, Entity at) {
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
        if (zDiff < 0.0)
            yaw = yaw + (Math.abs(180 - yaw) * 2);

        EntityLiving handle = ((CraftLivingEntity) from).getHandle();
        NMS.look(handle, (float) yaw - 90, (float) pitch);
    }

    public static BlockFace getFacingDirection(float degrees) {
        return getFacingDirection(degrees, 10);
    }

    public static BlockFace getFacingDirection(float degrees, double leeway) {
        while (degrees < 0D) {
            degrees += 360D;
        }
        while (degrees > 360D) {
            degrees -= 360D;
        }
        if (isFacingWest(degrees, leeway))
            return BlockFace.WEST;
        if (isFacingNorth(degrees, leeway))
            return BlockFace.NORTH;
        if (isFacingEast(degrees, leeway))
            return BlockFace.EAST;
        if (isFacingSouth(degrees, leeway))
            return BlockFace.SOUTH;
        return BlockFace.SELF;
    }

    private static boolean isFacingEast(double degrees, double leeway) {
        return (135 - leeway <= degrees) && (degrees < 225 + leeway);
    }

    private static boolean isFacingNorth(double degrees, double leeway) {
        return (45 - leeway <= degrees) && (degrees < 135 + leeway);
    }

    private static boolean isFacingSouth(double degrees, double leeway) {
        return (225 - leeway <= degrees) && (degrees < 315 + leeway);
    }

    private static boolean isFacingWest(double degrees, double leeway) {
        return ((0 <= degrees) && (degrees < 45 + leeway)) || ((315 - leeway <= degrees) && (degrees <= 360));
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

    public static void sendPacketNearby(Location location, Packet packet) {
        sendPacketNearby(location, packet, 64);
    }

    public static void sendPacketNearby(Location location, Packet packet, double radius) {
        radius *= radius;
        final World world = location.getWorld();
        for (Player ply : Bukkit.getServer().getOnlinePlayers()) {
            if (ply == null || world != ply.getWorld()) {
                continue;
            }
            if (location.distanceSquared(ply.getLocation()) > radius) {
                continue;
            }
            ((CraftPlayer) ply).getHandle().netServerHandler.sendPacket(packet);
        }
    }

    public static void sendToOnline(Packet... packets) {
        Validate.notNull(packets, "packets cannot be null");
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline())
                continue;
            for (Packet packet : packets) {
                ((CraftPlayer) player).getHandle().netServerHandler.sendPacket(packet);
            }
        }
    }
}
