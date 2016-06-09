package net.citizensnpcs.util;

import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.ArmorStandTrait;
import net.minecraft.server.v1_10_R1.BlockPosition;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.EnumHand;
import net.minecraft.server.v1_10_R1.Packet;
import net.minecraft.server.v1_10_R1.PacketPlayOutAnimation;
import net.minecraft.server.v1_10_R1.PacketPlayOutBed;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityMetadata;

public enum PlayerAnimation {
    ARM_SWING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            playDefaultAnimation(player, radius, 0);
        }
    },
    ARM_SWING_OFFHAND {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            playDefaultAnimation(player, radius, 3);
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
            final NPC holder = CitizensAPI.getNPCRegistry().createNPC(EntityType.ARMOR_STAND, "");
            holder.spawn(player.getBukkitEntity().getLocation());
            ArmorStandTrait trait = holder.getTrait(ArmorStandTrait.class);
            trait.setGravity(false);
            trait.setHasArms(false);
            trait.setHasBaseplate(false);
            trait.setSmall(true);
            trait.setMarker(true);
            trait.setVisible(false);
            holder.getTrait(ArmorStandTrait.class).setVisible(false);
            holder.data().set(NPC.NAMEPLATE_VISIBLE_METADATA, false);
            holder.data().set(NPC.DEFAULT_PROTECTED_METADATA, true);
            new BukkitRunnable() {
                @Override
                public void cancel() {
                    super.cancel();
                    holder.destroy();
                }

                @Override
                public void run() {
                    if (player.dead || !player.valid
                            || !player.getBukkitEntity().getMetadata("citizens.sitting").get(0).asBoolean()) {
                        cancel();
                        return;
                    }
                    if (player instanceof NPCHolder && !((NPCHolder) player).getNPC().isSpawned()) {
                        cancel();
                        return;
                    }
                    if (!NMS.getHandle(holder.getEntity()).passengers.contains(player)) {
                        NMS.mount(holder.getEntity(), player.getBukkitEntity());
                    }
                }
            }.runTaskTimer(CitizensAPI.getPlugin(), 0, 1);
        }
    },
    SLEEP {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            PacketPlayOutBed packet = new PacketPlayOutBed(player,
                    new BlockPosition((int) player.locX, (int) player.locY, (int) player.locZ));
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
    START_USE_MAINHAND_ITEM {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.c(EnumHand.MAIN_HAND);
            sendPacketNearby(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true), player,
                    radius);
        }
    },
    START_USE_OFFHAND_ITEM {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.c(EnumHand.OFF_HAND);
            sendPacketNearby(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true), player,
                    radius);
        }
    },
    STOP_SITTING {
        @Override
        protected void playAnimation(EntityPlayer player, int radius) {
            player.getBukkitEntity().setMetadata("citizens.sitting",
                    new FixedMetadataValue(CitizensAPI.getPlugin(), false));
            NMS.mount(player.getBukkitEntity(), null);
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
            player.cA();
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

    protected void sendPacketNearby(Packet<?> packet, EntityPlayer player, int radius) {
        NMS.sendPacketNearby(player.getBukkitEntity(), player.getBukkitEntity().getLocation(), packet, radius);
    }
}
