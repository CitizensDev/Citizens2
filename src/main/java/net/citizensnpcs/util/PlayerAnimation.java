package net.citizensnpcs.util;

import java.util.Arrays;

import net.minecraft.server.v1_7_R1.EntityPlayer;
import net.minecraft.server.v1_7_R1.Packet;
import net.minecraft.server.v1_7_R1.PacketPlayOutAnimation;
import net.minecraft.server.v1_7_R1.PacketPlayOutBed;
import net.minecraft.server.v1_7_R1.PacketPlayOutEntityMetadata;

import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public enum PlayerAnimation {
    ARM_SWING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            PacketPlayOutAnimation packet = new PacketPlayOutAnimation(player, 1);
            sendPacketNearby(packet, player, radius);
        }
    },
    CRIT {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            PacketPlayOutAnimation packet = new PacketPlayOutAnimation(player, 6);
            sendPacketNearby(packet, player, radius);
        }
    },
    HURT {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            PacketPlayOutAnimation packet = new PacketPlayOutAnimation(player, 2);
            sendPacketNearby(packet, player, radius);
        }
    },
    MAGIC_CRIT {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            PacketPlayOutAnimation packet = new PacketPlayOutAnimation(player, 7);
            sendPacketNearby(packet, player, radius);
        }
    },
    SIT {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.mount(player);
        }
    },
    SLEEP {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            PacketPlayOutBed packet = new PacketPlayOutBed(player, (int) player.locX, (int) player.locY,
                    (int) player.locZ);
            sendPacketNearby(packet, player, radius);
        }
    },
    SNEAK {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.getBukkitEntity().setSneaking(true);
            sendPacketNearby(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true), player,
                    radius);
        }
    },
    STOP_SITTING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.mount(null);
        }
    },
    STOP_SLEEPING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            PacketPlayOutAnimation packet = new PacketPlayOutAnimation(player, 3);
            sendPacketNearby(packet, player, radius);
        }
    },
    STOP_SNEAKING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.getBukkitEntity().setSneaking(false);
            sendPacketNearby(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true), player,
                    radius);
        }
    };

    public void play(Player player) {
        play(player, 64);
    }

    public void play(Player player, int radius) {
        playAnimation(((CraftPlayer) player).getHandle(), radius);
    }

    protected void playAnimation(EntityPlayer player, int radius) {
        throw new UnsupportedOperationException("unimplemented animation");
    }

    protected void sendPacketNearby(Packet packet, EntityPlayer player, int radius) {
        NMS.sendPacketsNearby(player.getBukkitEntity().getLocation(), Arrays.asList(packet), radius);
    }
}
