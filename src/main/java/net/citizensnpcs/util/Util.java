package net.citizensnpcs.util;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.minecraft.server.Packet;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
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
        Bukkit.getPluginManager().callEvent(event);
        return event;
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
            if (check.name().matches(toMatch) || check.name().replace('_', '-').equals(toMatch)) {
                type = check;
                break;
            }
        }
        return type;
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
