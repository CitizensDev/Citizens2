package net.citizensnpcs.util;

import net.citizensnpcs.Settings.Setting;
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
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.google.common.base.Splitter;

public class Util {

    // Static class for small (emphasis small) utility methods
    private Util() {
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
        if (zDiff < 0.0) {
            yaw = yaw + (Math.abs(180 - yaw) * 2);
        }

        EntityLiving handle = ((CraftLivingEntity) from).getHandle();
        handle.yaw = (float) yaw - 90;
        handle.pitch = (float) pitch;
        handle.as = handle.yaw;
    }
    
    public static void assumePosition(Entity entity, Position position) {
    	EntityLiving handle = ((CraftLivingEntity) entity).getHandle();
		handle.yaw = (float) position.getYaw();
		handle.pitch = (float) position.getPitch();
		handle.as = handle.yaw;
    }

    public static boolean isSettingFulfilled(Player player, Setting setting) {
        String parts = setting.asString();
        if (parts.contains("*"))
            return true;
        for (String part : Splitter.on(',').split(parts)) {
            if (Material.matchMaterial(part) == player.getItemInHand().getType()) {
                return true;
            }
        }
        return false;
    }

    public static EntityType matchEntityType(String toMatch) {
        EntityType type = EntityType.fromName(toMatch);
        if (type != null)
            return type;
        for (EntityType check : EntityType.values()) {
            if (check.name().matches(toMatch) || check.name().replace('_', '-').equalsIgnoreCase(toMatch)) {
                type = check;
                break;
            }
        }
        return type;
    }

    public static boolean rayTrace(LivingEntity entity, LivingEntity entity2) {
        EntityLiving from = ((CraftLivingEntity) entity).getHandle();
        EntityLiving to = ((CraftLivingEntity) entity2).getHandle();
        return from.l(to);
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
