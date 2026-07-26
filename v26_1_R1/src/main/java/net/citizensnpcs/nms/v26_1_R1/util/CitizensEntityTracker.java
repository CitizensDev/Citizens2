package net.citizensnpcs.nms.v26_1_R1.util;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import com.google.common.collect.ForwardingSet;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLinkToPlayerEvent;
import net.citizensnpcs.api.event.NPCSeenByPlayerEvent;
import net.citizensnpcs.api.event.NPCUnlinkFromPlayerEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.nms.v26_1_R1.entity.EntityHumanNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkMap.TrackedEntity;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public class CitizensEntityTracker extends ChunkMap.TrackedEntity {
    private final Set<UUID> foliaPendingUpdates = ConcurrentHashMap.newKeySet();
    private final Set<ServerPlayerConnection> rawSeenBy;
    private final Entity tracker;

    public CitizensEntityTracker(ChunkMap map, Entity entity, int i, int j, boolean flag) {
        map.super(entity, i, j, flag);
        this.rawSeenBy = seenBy;
        this.tracker = entity;
        try {
            TRACKING_SET_SETTER.invoke(this, new ForwardingSet<ServerPlayerConnection>() {
                @Override
                public boolean add(ServerPlayerConnection conn) {
                    boolean res = super.add(conn);
                    if (res) {
                        Bukkit.getPluginManager().callEvent(new NPCLinkToPlayerEvent(((NPCHolder) tracker).getNPC(),
                                conn.getPlayer().getBukkitEntity(), !Bukkit.isPrimaryThread()));
                    }
                    return res;
                }

                @Override
                protected Set<ServerPlayerConnection> delegate() {
                    return rawSeenBy;
                }

                @Override
                public boolean remove(Object conn) {
                    boolean removed = super.remove(conn);
                    if (removed) {
                        Bukkit.getPluginManager().callEvent(new NPCUnlinkFromPlayerEvent(((NPCHolder) tracker).getNPC(),
                                ((ServerPlayerConnection) conn).getPlayer().getBukkitEntity()));
                    }
                    return removed;
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public CitizensEntityTracker(ChunkMap map, TrackedEntity entry) {
        this(map, getTracker(entry), getTrackingDistance(entry), getUpdateInterval(entry), getTrackDelta(entry));
    }

    private void cancellableUpdatePlayer(final NPC npc, final ServerPlayer entityplayer,
            final java.util.function.Consumer<Boolean> callback) {
        CitizensAPI.getScheduler().checkedRunEntityTask(entityplayer.getBukkitEntity(), () -> {
            callback.accept(isUpdateCancelled(npc, entityplayer));
        });
    }

    private boolean isUpdateCancelled(final NPC npc, final ServerPlayer entityplayer) {
        NPCSeenByPlayerEvent event = new NPCSeenByPlayerEvent(npc, entityplayer.getBukkitEntity());
        try {
            Bukkit.getPluginManager().callEvent(event);
        } catch (IllegalStateException e) {
            REQUIRES_SYNC = true;
            throw e;
        }
        if (event.isCancelled()) {
            return true;
        }
        Integer trackingRange = npc.data().get(NPC.Metadata.TRACKING_RANGE);
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
    }

    private void updatePlayerFolia(final ServerPlayer entityplayer) {
        UUID playerId = entityplayer.getUUID();
        if (!foliaPendingUpdates.add(playerId)) {
            return;
        }
        CitizensAPI.getScheduler().checkedRunEntityTask(entityplayer.getBukkitEntity(), () -> {
            try {
                if (tracker.isRemoved()) {
                    return;
                }
                if (!seenBy.contains(entityplayer.connection) && tracker instanceof NPCHolder
                        && isUpdateCancelled(((NPCHolder) tracker).getNPC(), entityplayer)) {
                    return;
                }
                super.updatePlayer(entityplayer);
            } finally {
                foliaPendingUpdates.remove(playerId);
            }
        });
    }

    @Override
    public void removePlayer(final ServerPlayer entityplayer) {
        if (!SpigotUtil.isFoliaServer()) {
            super.removePlayer(entityplayer);
            return;
        }
        CitizensAPI.getScheduler().checkedRunEntityTask(entityplayer.getBukkitEntity(),
                () -> CitizensEntityTracker.super.removePlayer(entityplayer));
    }

    @Override
    public void updatePlayer(final ServerPlayer entityplayer) {
        if (entityplayer instanceof EntityHumanNPC)
            return;

        if (SpigotUtil.isFoliaServer()) {
            updatePlayerFolia(entityplayer);
            return;
        }
        if (!tracker.isRemoved() && !seenBy.contains(entityplayer.connection) && tracker instanceof NPCHolder) {
            NPC npc = ((NPCHolder) tracker).getNPC();
            if (REQUIRES_SYNC == null) {
                REQUIRES_SYNC = !Bukkit.isPrimaryThread();
            }
            cancellableUpdatePlayer(npc, entityplayer, cancelled -> {
                if (cancelled) {
                    return;
                }
                super.updatePlayer(entityplayer);
            });
            return;
        }
        super.updatePlayer(entityplayer);
    }

    public static Collection<org.bukkit.entity.Entity> getSeenBy(TrackedEntity tracker) {
        return tracker.seenBy.stream().map(c -> c.getPlayer().getBukkitEntity()).collect(Collectors.toSet());
    }

    static void transferSeenBy(TrackedEntity previous, CitizensEntityTracker replacement) {
        replacement.rawSeenBy.addAll(previous.seenBy);
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
    private static final MethodHandle TRACK_DELTA = NMSImpl.SERVER_ENTITY_TRACK_DELTA;
    private static final MethodHandle TRACKER = NMS.getFirstGetter(TrackedEntity.class, Entity.class);
    private static final MethodHandle TRACKER_ENTRY = NMS.getFirstGetter(TrackedEntity.class, ServerEntity.class);
    private static final MethodHandle TRACKING_RANGE = NMS.getFirstGetter(TrackedEntity.class, int.class);
    private static final MethodHandle TRACKING_RANGE_SETTER = NMS.getFirstFinalSetter(TrackedEntity.class, int.class);
    private static final MethodHandle TRACKING_SET_SETTER = NMS.getFirstFinalSetter(TrackedEntity.class, Set.class);
    private static final MethodHandle UPDATE_INTERVAL = NMSImpl.SERVER_ENTITY_UPDATE_INTERVAL;
}
