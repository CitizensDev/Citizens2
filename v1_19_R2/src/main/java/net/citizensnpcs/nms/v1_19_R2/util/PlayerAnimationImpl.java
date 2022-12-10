package net.citizensnpcs.nms.v1_19_R2.util;

import java.util.Map;

import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

import net.citizensnpcs.util.PlayerAnimation;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class PlayerAnimationImpl {
    public static void play(PlayerAnimation animation, Player bplayer, int radius) {
        final ServerPlayer player = (ServerPlayer) NMSImpl.getHandle(bplayer);
        if (DEFAULTS.containsKey(animation)) {
            playDefaultAnimation(player, radius, DEFAULTS.get(animation));
            return;
        }
        switch (animation) {
            case SNEAK:
                player.getBukkitEntity().setSneaking(true);
                sendEntityData(radius, player);
                break;
            case START_ELYTRA:
                player.startFallFlying();
                break;
            case STOP_ELYTRA:
                player.stopFallFlying();
                break;
            case START_USE_MAINHAND_ITEM:
                player.startUsingItem(InteractionHand.MAIN_HAND);
                sendEntityData(radius, player);
                break;
            case START_USE_OFFHAND_ITEM:
                player.startUsingItem(InteractionHand.OFF_HAND);
                sendEntityData(radius, player);
                break;
            case STOP_SNEAKING:
                player.getBukkitEntity().setSneaking(false);
                sendEntityData(radius, player);
                break;
            case STOP_USE_ITEM:
                player.stopUsingItem();
                sendEntityData(radius, player);
                break;
            default:
                throw new UnsupportedOperationException();
        }

    }

    protected static void playDefaultAnimation(ServerPlayer player, int radius, int code) {
        ClientboundAnimatePacket packet = new ClientboundAnimatePacket(player, code);
        sendPacketNearby(packet, player, radius);
    }

    private static void sendEntityData(int radius, final ServerPlayer player) {
        if (!player.getEntityData().isDirty())
            return;
        sendPacketNearby(new ClientboundSetEntityDataPacket(player.getId(), player.getEntityData().packDirty()), player,
                radius);
    }

    protected static void sendPacketNearby(Packet<?> packet, ServerPlayer player, int radius) {
        NMSImpl.sendPacketNearby(player.getBukkitEntity(), player.getBukkitEntity().getLocation(), packet, radius);
    }

    private static Map<PlayerAnimation, Integer> DEFAULTS = Maps.newEnumMap(PlayerAnimation.class);
    static {
        DEFAULTS.put(PlayerAnimation.ARM_SWING, 0);
        DEFAULTS.put(PlayerAnimation.HURT, 1);
        DEFAULTS.put(PlayerAnimation.LEAVE_BED, 2);
        DEFAULTS.put(PlayerAnimation.ARM_SWING_OFFHAND, 3);
        DEFAULTS.put(PlayerAnimation.CRIT, 4);
        DEFAULTS.put(PlayerAnimation.MAGIC_CRIT, 5);
    }
}
