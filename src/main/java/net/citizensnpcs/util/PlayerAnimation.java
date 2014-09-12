package net.citizensnpcs.util;

import java.util.Arrays;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayOutAnimation;
import net.minecraft.server.v1_7_R4.PacketPlayOutBed;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityMetadata;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public enum PlayerAnimation {
    ARM_SWING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            playDefaultAnimation(player, radius, 0);
        }
    },
    CRIT {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            playDefaultAnimation(player, radius, 4);
        }
    },
    EAT_FOOD {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            playDefaultAnimation(player, radius, 3);
        }
    },
    HURT {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            playDefaultAnimation(player, radius, 1);
        }
    },
    MAGIC_CRIT {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            playDefaultAnimation(player, radius, 5);
        }
    },
    SIT {
        @Override
        protected void playAnimation(final EntityPlayer player, int radius) {
            player.getBukkitEntity().setMetadata("citizens.sitting",
                    new FixedMetadataValue(CitizensAPI.getPlugin(), true));
            Bukkit.getScheduler().runTaskTimer(CitizensAPI.getPlugin(), new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.dead || !player.getBukkitEntity().getMetadata("citizens.sitting").get(0).asBoolean()) {
                        cancel();
                        return;
                    }
                    if (player instanceof NPCHolder && !((NPCHolder) player).getNPC().isSpawned()) {
                        cancel();
                        return;
                    }
                    if (player.passenger != player) {
                        player.mount(player);
                    }
                }
            }, 0, 1);
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
    START_USE_ITEM {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.e(true);
            sendPacketNearby(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true), player,
                    radius);
        }
    },
    STOP_SITTING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.getBukkitEntity().setMetadata("citizens.sitting",
                    new FixedMetadataValue(CitizensAPI.getPlugin(), false));
            player.mount(null);
        }
    },
    STOP_SLEEPING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            playDefaultAnimation(player, radius, 2);
        }
    },
    STOP_SNEAKING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.getBukkitEntity().setSneaking(false);
            sendPacketNearby(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true), player,
                    radius);
        }
    },
    STOP_USE_ITEM {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.e(false);
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

    protected void playDefaultAnimation(EntityPlayer player, int radius, int code) {
        PacketPlayOutAnimation packet = new PacketPlayOutAnimation(player, code);
        sendPacketNearby(packet, player, radius);
    }

    protected void sendPacketNearby(Packet packet, EntityPlayer player, int radius) {
        NMS.sendPacketsNearby(player.getBukkitEntity(), player.getBukkitEntity().getLocation(), Arrays.asList(packet),
                radius);
    }
}
