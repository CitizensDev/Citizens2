package net.citizensnpcs.util;

import java.util.Arrays;

import net.minecraft.server.v1_5_R3.EntityPlayer;
import net.minecraft.server.v1_5_R3.Packet;
import net.minecraft.server.v1_5_R3.Packet17EntityLocationAction;
import net.minecraft.server.v1_5_R3.Packet18ArmAnimation;
import net.minecraft.server.v1_5_R3.Packet40EntityMetadata;

import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public enum PlayerAnimation {
    ARM_SWING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            Packet18ArmAnimation packet = new Packet18ArmAnimation(player, 1);
            sendPacketNearby(packet, player, radius);
        }
    },
    HURT {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            Packet18ArmAnimation packet = new Packet18ArmAnimation(player, 2);
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
            Packet17EntityLocationAction packet = new Packet17EntityLocationAction(player, 0, (int) player.locX,
                    (int) player.locY, (int) player.locZ);
            sendPacketNearby(packet, player, radius);
        }
    },
    SNEAK {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.getBukkitEntity().setSneaking(true);
            sendPacketNearby(new Packet40EntityMetadata(player.id, player.getDataWatcher(), true), player, radius);
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
            Packet18ArmAnimation packet = new Packet18ArmAnimation(player, 3);
            sendPacketNearby(packet, player, radius);
        }
    },
    STOP_SNEAKING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.getBukkitEntity().setSneaking(false);
            sendPacketNearby(new Packet40EntityMetadata(player.id, player.getDataWatcher(), true), player, radius);
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
