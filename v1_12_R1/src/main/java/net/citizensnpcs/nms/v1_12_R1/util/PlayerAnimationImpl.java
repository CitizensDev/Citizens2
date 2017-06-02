package net.citizensnpcs.nms.v1_12_R1.util;

import java.util.EnumMap;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.ArmorStandTrait;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerAnimation;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumHand;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutAnimation;
import net.minecraft.server.v1_12_R1.PacketPlayOutBed;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata;

public class PlayerAnimationImpl {
    public static void play(PlayerAnimation animation, Player bplayer, int radius) {
        // TODO: this is pretty gross
        final EntityPlayer player = (EntityPlayer) NMSImpl.getHandle(bplayer);
        if (DEFAULTS.containsKey(animation)) {
            playDefaultAnimation(player, radius, DEFAULTS.get(animation));
            return;
        }
        switch (animation) {
            case SIT:
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
                        if (!NMS.getPassengers(holder.getEntity()).contains(player.getBukkitEntity())) {
                            NMS.mount(holder.getEntity(), player.getBukkitEntity());
                        }
                    }
                }.runTaskTimer(CitizensAPI.getPlugin(), 0, 1);
                break;
            case SLEEP:
                PacketPlayOutBed packet = new PacketPlayOutBed(player,
                        new BlockPosition((int) player.locX, (int) player.locY, (int) player.locZ));
                sendPacketNearby(packet, player, radius);
                break;
            case SNEAK:
                player.getBukkitEntity().setSneaking(true);
                sendPacketNearby(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true), player,
                        radius);
                break;
            case START_USE_MAINHAND_ITEM:
                player.c(EnumHand.MAIN_HAND);
                sendPacketNearby(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true), player,
                        radius);
                break;
            case START_USE_OFFHAND_ITEM:
                player.c(EnumHand.OFF_HAND);
                sendPacketNearby(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true), player,
                        radius);
                break;
            case STOP_SITTING:
                player.getBukkitEntity().setMetadata("citizens.sitting",
                        new FixedMetadataValue(CitizensAPI.getPlugin(), false));
                NMS.mount(player.getBukkitEntity(), null);
                break;
            case STOP_SLEEPING:
                playDefaultAnimation(player, radius, 2);
                break;
            case STOP_SNEAKING:
                player.getBukkitEntity().setSneaking(false);
                sendPacketNearby(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true), player,
                        radius);
                break;
            case STOP_USE_ITEM:
                player.clearActiveItem();
                sendPacketNearby(new PacketPlayOutEntityMetadata(player.getId(), player.getDataWatcher(), true), player,
                        radius);
                break;
            default:
                break;
        }
    }

    protected static void playDefaultAnimation(EntityPlayer player, int radius, int code) {
        PacketPlayOutAnimation packet = new PacketPlayOutAnimation(player, code);
        sendPacketNearby(packet, player, radius);
    }

    protected static void sendPacketNearby(Packet<?> packet, EntityPlayer player, int radius) {
        NMSImpl.sendPacketNearby(player.getBukkitEntity(), player.getBukkitEntity().getLocation(), packet, radius);
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
