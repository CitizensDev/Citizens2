package net.citizensnpcs.nms.v1_8_R3.util;

import java.util.EnumMap;

import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

import net.citizensnpcs.util.PlayerAnimation;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;

public class PlayerAnimationImpl {
    public static void play(PlayerAnimation animation, Player bplayer, Iterable<Player> to) {
        // TODO: this is pretty gross
        final EntityPlayer player = (EntityPlayer) NMSImpl.getHandle(bplayer);
        if (DEFAULTS.containsKey(animation)) {
            playDefaultAnimation(player, to, DEFAULTS.get(animation));
            return;
        }
        switch (animation) {
            case SNEAK:
                player.getBukkitEntity().setSneaking(true);
                sendPacketTo(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true), to);
                break;
            case STOP_SNEAKING:
                player.getBukkitEntity().setSneaking(false);
                sendPacketTo(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true), to);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected static void playDefaultAnimation(EntityPlayer player, Iterable<Player> to, int code) {
        PacketPlayOutAnimation packet = new PacketPlayOutAnimation(player, code);
        sendPacketTo(packet, to);
    }

    protected static void sendPacketTo(Packet<?> packet, Iterable<Player> to) {
        for (Player player : to) {
            NMSImpl.sendPacket(player, packet);
        }
    }

    private static EnumMap<PlayerAnimation, Integer> DEFAULTS = Maps.newEnumMap(PlayerAnimation.class);
    static {
        DEFAULTS.put(PlayerAnimation.ARM_SWING, 0);
        DEFAULTS.put(PlayerAnimation.HURT, 1);
        DEFAULTS.put(PlayerAnimation.EAT_FOOD, 2);
        DEFAULTS.put(PlayerAnimation.ARM_SWING_OFFHAND, 3);
        DEFAULTS.put(PlayerAnimation.CRIT, 4);
        DEFAULTS.put(PlayerAnimation.MAGIC_CRIT, 5);
    }
}
