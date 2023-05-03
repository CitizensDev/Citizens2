package net.citizensnpcs.nms.v1_15_R1.util;

import java.lang.invoke.MethodHandle;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.collect.ForwardingSet;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCSeenByPlayerEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_15_R1.entity.EntityHumanNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EntityTrackerEntry;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_15_R1.PlayerChunkMap;
import net.minecraft.server.v1_15_R1.PlayerChunkMap.EntityTracker;

public class PlayerlistTracker extends PlayerChunkMap.EntityTracker {
    private final Entity tracker;

    public PlayerlistTracker(PlayerChunkMap map, Entity entity, int i, int j, boolean flag) {
        map.super(entity, i, j, flag);
        this.tracker = entity;
        try {
            Set<EntityPlayer> delegate = super.trackedPlayers;
            TRACKING_SET_SETTER.invoke(this, new ForwardingSet<EntityPlayer>() {
                @Override
                public boolean add(EntityPlayer player) {
                    boolean res = super.add(player);
                    if (res) {
                        updateLastPlayer(player);
                    }
                    return res;
                }

                @Override
                protected Set<EntityPlayer> delegate() {
                    return delegate;
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public PlayerlistTracker(PlayerChunkMap map, EntityTracker entry) {
        this(map, getTracker(entry), getTrackingDistance(entry), getD(entry), getE(entry));
    }

    public void updateLastPlayer(EntityPlayer lastUpdatedPlayer) {
        if (tracker.dead || lastUpdatedPlayer == null || tracker.getBukkitEntity().getType() != EntityType.PLAYER)
            return;
        final EntityPlayer entityplayer = lastUpdatedPlayer;
        NMS.sendTabListAdd(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
            NMSImpl.sendPacket(entityplayer.getBukkitEntity(),
                    new PacketPlayOutEntityLook(tracker.getId(), (byte) (tracker.yaw * 256.0F / 360.0F),
                            (byte) (tracker.pitch * 256.0F / 360.0F), tracker.onGround));
        }, 1);
        if (!Setting.DISABLE_TABLIST.asBoolean())
            return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(),
                () -> NMS.sendTabListRemove(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity()),
                Setting.TABLIST_REMOVE_PACKET_DELAY.asTicks());
    }

    @Override
    public void updatePlayer(final EntityPlayer entityplayer) {
        if (tracker instanceof NPCHolder) {
            NPC npc = ((NPCHolder) tracker).getNPC();
            NPCSeenByPlayerEvent event = new NPCSeenByPlayerEvent(npc, entityplayer.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                return;
            Integer trackingRange = npc.data().<Integer> get(NPC.Metadata.TRACKING_RANGE);
            if (TRACKING_RANGE_SETTER != null && trackingRange != null
                    && npc.data().get("last-tracking-range", -1) != trackingRange.intValue()) {
                try {
                    TRACKING_RANGE_SETTER.invoke(this, trackingRange);
                    npc.data().set("last-tracking-range", trackingRange);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        if (entityplayer instanceof EntityHumanNPC)
            return;

        super.updatePlayer(entityplayer);
    }

    private static int getD(EntityTracker entry) {
        try {
            return (int) D.invoke(TRACKER_ENTRY.invoke(entry));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static boolean getE(EntityTracker entry) {
        try {
            return (boolean) E.invoke(TRACKER_ENTRY.invoke(entry));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Entity getTracker(EntityTracker entry) {
        try {
            return (Entity) TRACKER.invoke(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int getTrackingDistance(EntityTracker entry) {
        try {
            return (Integer) TRACKING_RANGE.invoke(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static final MethodHandle D = NMS.getGetter(EntityTrackerEntry.class, "d");
    private static final MethodHandle E = NMS.getGetter(EntityTrackerEntry.class, "e");
    private static final MethodHandle TRACKER = NMS.getGetter(EntityTracker.class, "tracker");
    private static final MethodHandle TRACKER_ENTRY = NMS.getGetter(EntityTracker.class, "trackerEntry");
    private static final MethodHandle TRACKING_RANGE = NMS.getGetter(EntityTracker.class, "trackingDistance");
    private static final MethodHandle TRACKING_RANGE_SETTER = NMS.getFirstFinalSetter(EntityTracker.class, int.class);
    private static final MethodHandle TRACKING_SET_SETTER = NMS.getFirstFinalSetter(EntityTracker.class, Set.class);
}
