package net.citizensnpcs.util;

import java.util.Random;

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

import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;

public class Util {
    // Static class for small (emphasis small) utility methods
    private Util() {
    }

    public static void assumePose(Entity entity, float yaw, float pitch) {
        NMS.look(entity, yaw, pitch);
    }

    public static void callCollisionEvent(NPC npc, Entity entity) {
        if (NPCCollisionEvent.getHandlerList().getRegisteredListeners().length > 0) {
            Bukkit.getPluginManager().callEvent(new NPCCollisionEvent(npc, entity));
        }
    }

    public static NPCPushEvent callPushEvent(NPC npc, Vector vector) {
        NPCPushEvent event = new NPCPushEvent(npc, vector);
        event.setCancelled(npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static float clampYaw(float yaw) {
        while (yaw < -180.0F) {
            yaw += 360.0F;
        }

        while (yaw >= 180.0F) {
            yaw -= 360.0F;
        }
        return yaw;
    }

    public static void faceEntity(Entity entity, Entity at) {
        if (at == null || entity == null || entity.getWorld() != at.getWorld())
            return;
        if (at instanceof LivingEntity) {
            NMS.look(entity, at);
        } else {
            faceLocation(entity, at.getLocation(AT_LOCATION));
        }
    }

    public static void faceLocation(Entity entity, Location to) {
        faceLocation(entity, to, false);
    }

    public static void faceLocation(Entity entity, Location to, boolean headOnly) {
        faceLocation(entity, to, headOnly, true);
    }

    public static void faceLocation(Entity entity, Location to, boolean headOnly, boolean immediate) {
        if (to == null || entity.getWorld() != to.getWorld())
            return;
        NMS.look(entity, to, headOnly, immediate);
    }

    public static Location getEyeLocation(Entity entity) {
        return entity instanceof LivingEntity ? ((LivingEntity) entity).getEyeLocation() : entity.getLocation();
    }

    public static Random getFastRandom() {
        return new XORShiftRNG();
    }

    public static String getMinecraftRevision() {
        String raw = Bukkit.getServer().getClass().getPackage().getName();
        return raw.substring(raw.lastIndexOf('.') + 2);
    }

    public static boolean isAlwaysFlyable(EntityType type) {
        if (type.name().toLowerCase().contains("vex")) // 1.11 compatibility
            return true;
        switch (type) {
            case BAT:
            case BLAZE:
            case ENDER_DRAGON:
            case GHAST:
            case PARROT:
            case WITHER:
                return true;
            default:
                return false;
        }
    }

    public static boolean isLoaded(Location location) {
        if (location.getWorld() == null)
            return false;
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return location.getWorld().isChunkLoaded(chunkX, chunkZ);
    }

    public static String listValuesPretty(Enum<?>[] values) {
        return "<e>" + Joiner.on("<a>, <e>").join(values).toLowerCase().replace('_', ' ');
    }

    public static boolean locationWithinRange(Location current, Location target, double range) {
        if (current == null || target == null)
            return false;
        if (current.getWorld() != target.getWorld())
            return false;
        return current.distanceSquared(target) < Math.pow(range, 2);
    }

    public static EntityType matchEntityType(String toMatch) {
        return matchEnum(EntityType.values(), toMatch);
    }

    public static <T extends Enum<?>> T matchEnum(T[] values, String toMatch) {
        toMatch = toMatch.toLowerCase().replace('-', '_').replace(' ', '_');
        for (T check : values) {
            if (toMatch.equals(check.name().toLowerCase())
                    || (toMatch.equals("item") && check == EntityType.DROPPED_ITEM)) {
                return check; // check for an exact match first
            }
        }
        for (T check : values) {
            String name = check.name().toLowerCase();
            if (name.replace("_", "").equals(toMatch) || name.startsWith(toMatch)) {
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
            if (Material.matchMaterial(part) == player.getInventory().getItemInMainHand().getType()) {
                return true;
            }
        }
        return false;
    }

    public static String prettyEnum(Enum<?> e) {
        return e.name().toLowerCase().replace('_', ' ');
    }

    private static final Location AT_LOCATION = new Location(null, 0, 0, 0);
}
