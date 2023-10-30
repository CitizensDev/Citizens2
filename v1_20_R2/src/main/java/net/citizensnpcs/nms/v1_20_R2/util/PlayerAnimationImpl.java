package net.citizensnpcs.nms.v1_20_R2.util;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.util.PlayerAnimation;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;

public class PlayerAnimationImpl {
    public static void play(PlayerAnimation animation, Player bplayer, Iterable<Player> to) {
        final ServerPlayer player = (ServerPlayer) NMSImpl.getHandle(bplayer);
        if (DEFAULTS.containsKey(animation)) {
            playDefaultAnimation(player, to, DEFAULTS.get(animation));
            return;
        }

        switch (animation) {
            case HURT:
                sendPacketNearby(new ClientboundHurtAnimationPacket(player), to);
                break;
            case SNEAK:
                player.setPose(Pose.CROUCHING);
                sendEntityData(to, player);
                break;
            case START_ELYTRA:
                player.startFallFlying();
                break;
            case STOP_ELYTRA:
                player.stopFallFlying();
                break;
            case START_USE_MAINHAND_ITEM:
                player.startUsingItem(InteractionHand.MAIN_HAND);
                sendEntityData(to, player);
                player.getBukkitEntity().setMetadata("citizens-using-item-remaining-ticks",
                        new FixedMetadataValue(CitizensAPI.getPlugin(), player.getUseItemRemainingTicks()));
                break;
            case START_USE_OFFHAND_ITEM:
                player.startUsingItem(InteractionHand.OFF_HAND);
                sendEntityData(to, player);
                player.getBukkitEntity().setMetadata("citizens-using-item-remaining-ticks",
                        new FixedMetadataValue(CitizensAPI.getPlugin(), player.getUseItemRemainingTicks()));
                break;
            case STOP_SNEAKING:
                player.setPose(Pose.STANDING);
                sendEntityData(to, player);
                break;
            case STOP_USE_ITEM:
                player.stopUsingItem();
                sendEntityData(to, player);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected static void playDefaultAnimation(ServerPlayer player, Iterable<Player> to, int code) {
        ClientboundAnimatePacket packet = new ClientboundAnimatePacket(player, code);
        sendPacketNearby(packet, to);
    }

    private static void sendEntityData(Iterable<Player> to, final ServerPlayer player) {
        if (!player.getEntityData().isDirty())
            return;
        sendPacketNearby(new ClientboundSetEntityDataPacket(player.getId(), player.getEntityData().packDirty()), to);
    }

    protected static void sendPacketNearby(Packet<?> packet, Iterable<Player> to) {
        for (Player player : to) {
            NMSImpl.sendPacket(player, packet);
        }
    }

    private static Map<PlayerAnimation, Integer> DEFAULTS = Maps.newEnumMap(PlayerAnimation.class);
    static {
        DEFAULTS.put(PlayerAnimation.ARM_SWING, 0);
        DEFAULTS.put(PlayerAnimation.LEAVE_BED, 2);
        DEFAULTS.put(PlayerAnimation.ARM_SWING_OFFHAND, 3);
        DEFAULTS.put(PlayerAnimation.CRIT, 4);
        DEFAULTS.put(PlayerAnimation.MAGIC_CRIT, 5);
    }
}
