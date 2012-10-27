package net.citizensnpcs.util;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet17EntityLocationAction;
import net.minecraft.server.Packet18ArmAnimation;
import net.minecraft.server.Packet40EntityMetadata;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public enum PlayerAnimation {
    HURT {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            Packet18ArmAnimation packet = new Packet18ArmAnimation(player, 2);
            sendPacketNearby(packet, player, radius);
        }
    },
    SLEEP {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            Packet17EntityLocationAction packet = new Packet17EntityLocationAction(player, 0,
                    (int) player.locX, (int) player.locY, (int) player.locZ);
            sendPacketNearby(packet, player, radius);
        }
    },
    SNEAK {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.getBukkitEntity().setSneaking(true);
            sendPacketNearby(new Packet40EntityMetadata(player.id, player.getDataWatcher()), player, radius);
        }
    },
    STOP_SLEEPING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            Packet18ArmAnimation packet = new Packet18ArmAnimation(player, 3);
            sendPacketNearby(packet, player, radius);
        }
    },
    STOP_SNEAKING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.getBukkitEntity().setSneaking(false);
            sendPacketNearby(new Packet40EntityMetadata(player.id, player.getDataWatcher()), player, radius);
        }
    },
    SWING_ARM {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            Packet18ArmAnimation packet = new Packet18ArmAnimation(player, 1);
            sendPacketNearby(packet, player, radius);
        }
    };

    public void play(Player player) {
        playAnimation(((CraftPlayer) player).getHandle(), 64);
    }

    protected void playAnimation(EntityPlayer player, int radius) {
        throw new UnsupportedOperationException("unimplemented animation");
    }

    protected void sendPacketNearby(Packet packet, EntityPlayer player, int radius) {
        radius *= radius;
        World world = player.world.getWorld();
        Location location = player.getBukkitEntity().getLocation();
        for (Player dest : Bukkit.getServer().getOnlinePlayers()) {
            if (dest == null || world != dest.getWorld())
                continue;
            if (location.distanceSquared(dest.getLocation()) > radius)
                continue;
            ((CraftPlayer) dest).getHandle().netServerHandler.sendPacket(packet);
        }
    }
}
