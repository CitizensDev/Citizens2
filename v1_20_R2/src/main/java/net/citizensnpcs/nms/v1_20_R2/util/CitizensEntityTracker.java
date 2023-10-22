package net.citizensnpcs.nms.v1_20_R2.util;

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
import net.citizensnpcs.nms.v1_20_R2.entity.EntityHumanNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkMap.TrackedEntity;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public class CitizensEntityTracker extends ChunkMap.TrackedEntity {
    private final Entity tracker;

    public CitizensEntityTracker(ChunkMap map, Entity entity, int i, int j, boolean flag) {
        map.super(entity, i, j, flag);
        this.tracker = entity;
        try {
            Set<ServerPlayerConnection> set = (Set<ServerPlayerConnection>) TRACKING_SET_GETTER.invoke(this);
            TRACKING_SET_SETTER.invoke(this, new ForwardingSet<ServerPlayerConnection>() {
                @Override
                public boolean add(ServerPlayerConnection conn) {
                    boolean res = super.add(conn);
                    if (res) {
                        updateLastPlayer(conn.getPlayer());
                    }
                    return res;
                }

                @Override
                protected Set<ServerPlayerConnection> delegate() {
                    return set;
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public CitizensEntityTracker(ChunkMap map, TrackedEntity entry) {
        this(map, getTracker(entry), getTrackingDistance(entry), getUpdateInterval(entry), getTrackDelta(entry));
    }

    public void updateLastPlayer(ServerPlayer lastUpdatedPlayer) {
        if (tracker.isRemoved() || tracker.getBukkitEntity().getType() != EntityType.PLAYER
                || !CitizensAPI.hasImplementation())
            return;
        final ServerPlayer entityplayer = lastUpdatedPlayer;
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
            if (tracker.isRemoved() || entityplayer.isRemoved())
                return;
            NMSImpl.sendPacket(entityplayer.getBukkitEntity(),
                    new ClientboundRotateHeadPacket(tracker, (byte) (tracker.getYHeadRot() * 256.0F / 360.0F)));
        }, Setting.TABLIST_REMOVE_PACKET_DELAY.asTicks() + 1);
        NPC npc = ((NPCHolder) tracker).getNPC();
        boolean resetYaw = npc.data().get(NPC.Metadata.RESET_YAW_ON_SPAWN, Setting.RESET_YAW_ON_SPAWN.asBoolean());
        boolean sendTabRemove = NMS.sendTabListAdd(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
        if (!sendTabRemove || !Setting.DISABLE_TABLIST.asBoolean()) {
            if (resetYaw) {
                Bukkit.getScheduler()
                        .scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> NMSImpl
                                .sendPacket(entityplayer.getBukkitEntity(), new ClientboundAnimatePacket(tracker, 0)),
                                1);
            }
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
            if (tracker.isRemoved() || entityplayer.isRemoved())
                return;
            NMS.sendTabListRemove(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
            if (resetYaw) {
                NMSImpl.sendPacket(entityplayer.getBukkitEntity(), new ClientboundAnimatePacket(tracker, 0));
            }
        }, Setting.TABLIST_REMOVE_PACKET_DELAY.asTicks());
    }

    @Override
    public void updatePlayer(final ServerPlayer entityplayer) {
        if (entityplayer instanceof EntityHumanNPC)
            return;

        if (!tracker.isRemoved() && !seenBy.contains(entityplayer.connection) && tracker instanceof NPCHolder) {
            NPC npc = ((NPCHolder) tracker).getNPC();
            if (REQUIRES_SYNC == null) {
                REQUIRES_SYNC = !Bukkit.isPrimaryThread();
            }

            boolean cancelled = Util.callPossiblySync(() -> {
                NPCSeenByPlayerEvent event = new NPCSeenByPlayerEvent(npc, entityplayer.getBukkitEntity());
                try {
                    Bukkit.getPluginManager().callEvent(event);
                } catch (IllegalStateException e) {
                    REQUIRES_SYNC = true;
                    throw e;
                }

                if (event.isCancelled())
                    return true;

                Integer trackingRange = npc.data().<Integer> get(NPC.Metadata.TRACKING_RANGE);
                if (TRACKING_RANGE_SETTER != null && trackingRange != null
                        && npc.data().get("last-tracking-range", -1) != trackingRange.intValue()) {
                    try {
                        TRACKING_RANGE_SETTER.invoke(CitizensEntityTracker.this, trackingRange);
                        npc.data().set("last-tracking-range", trackingRange);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }, REQUIRES_SYNC);

            if (cancelled)
                return;
        }

        super.updatePlayer(entityplayer);
    }

    private static boolean getTrackDelta(TrackedEntity entry) {
        try {
            return (boolean) TRACK_DELTA.invoke(TRACKER_ENTRY.invoke(entry));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Entity getTracker(TrackedEntity entry) {
        try {
            return (Entity) TRACKER.invoke(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int getTrackingDistance(TrackedEntity entry) {
        try {
            return (Integer) TRACKING_RANGE.invoke(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int getUpdateInterval(TrackedEntity entry) {
        try {
            return (int) UPDATE_INTERVAL.invoke(TRACKER_ENTRY.invoke(entry));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static volatile Boolean REQUIRES_SYNC;
    private static final MethodHandle TRACK_DELTA = NMS.getGetter(ServerEntity.class, "i");
    private static final MethodHandle TRACKER = NMS.getFirstGetter(TrackedEntity.class, Entity.class);
    private static final MethodHandle TRACKER_ENTRY = NMS.getFirstGetter(TrackedEntity.class, ServerEntity.class);
    private static final MethodHandle TRACKING_RANGE = NMS.getFirstGetter(TrackedEntity.class, int.class);
    private static final MethodHandle TRACKING_RANGE_SETTER = NMS.getFirstFinalSetter(TrackedEntity.class, int.class);
    private static final MethodHandle TRACKING_SET_GETTER = NMS.getFirstGetter(TrackedEntity.class, Set.class);
    private static final MethodHandle TRACKING_SET_SETTER = NMS.getFirstFinalSetter(TrackedEntity.class, Set.class);
    private static final MethodHandle UPDATE_INTERVAL = NMS.getGetter(ServerEntity.class, "h");
}
