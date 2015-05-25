package net.citizensnpcs.util.nms;

import java.lang.reflect.Field;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerlistTrackerEntry extends EntityTrackerEntry {
    public PlayerlistTrackerEntry(Entity entity, int i, int j, boolean flag) {
        super(entity, i, j, flag);
    }

    public PlayerlistTrackerEntry(EntityTrackerEntry entry) {
        this(entry.tracker, entry.b, entry.c, getU(entry));
    }

    @Override
    public void updatePlayer(final EntityPlayer entityplayer) {
        if (entityplayer != this.tracker && c(entityplayer)) {
            if (!this.trackedPlayers.contains(entityplayer)
                    && ((entityplayer.u().getPlayerChunkMap().a(entityplayer, this.tracker.ae, this.tracker.ag)) || (this.tracker.attachedToPlayer))) {
                if ((this.tracker instanceof EntityPlayer)) {
                    Player player = ((EntityPlayer) this.tracker).getBukkitEntity();
                    if (!entityplayer.getBukkitEntity().canSee(player)) {
                        return;
                    }
                    entityplayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                            PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, (EntityPlayer) this.tracker));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            entityplayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(
                                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, (EntityPlayer) tracker));
                        }
                    }.runTaskLater(CitizensAPI.getPlugin(), 2);
                }
            }
        }
        super.updatePlayer(entityplayer);
    }

    private static boolean getU(EntityTrackerEntry entry) {
        try {
            return (Boolean) U.get(entry);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Field U = NMS.getField(EntityTrackerEntry.class, "u");
}
