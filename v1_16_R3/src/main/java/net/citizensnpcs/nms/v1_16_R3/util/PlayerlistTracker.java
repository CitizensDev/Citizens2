package net.citizensnpcs.nms.v1_16_R3.util;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingSet;

import net.citizensnpcs.api.event.NPCLinkToPlayerEvent;
import net.citizensnpcs.api.event.NPCSeenByPlayerEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_16_R3.entity.EntityHumanNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EntityTrackerEntry;
import net.minecraft.server.v1_16_R3.PlayerChunkMap;
import net.minecraft.server.v1_16_R3.PlayerChunkMap.EntityTracker;

public class PlayerlistTracker extends PlayerChunkMap.EntityTracker {
    private final Entity tracker;
    private Map<EntityPlayer, Boolean> trackingMap;

    public PlayerlistTracker(PlayerChunkMap map, Entity entity, int i, int j, boolean flag) {
        map.super(entity, i, j, flag);
        this.tracker = entity;
        if (TRACKING_MAP_SETTER != null) {
            try {
                Map<EntityPlayer, Boolean> delegate = (Map<EntityPlayer, Boolean>) TRACKING_MAP_GETTER.invoke(this);
                trackingMap = delegate;
                TRACKING_MAP_SETTER.invoke(this, new ForwardingMap<EntityPlayer, Boolean>() {
                    @Override
                    protected Map<EntityPlayer, Boolean> delegate() {
                        return delegate;
                    }

                    @Override
                    public Boolean put(EntityPlayer player, Boolean value) {
                        Boolean res = super.put(player, value);
                        if (res == null) {
                            updateLastPlayer(player);
                        }
                        return res;
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
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
    }

    public PlayerlistTracker(PlayerChunkMap map, EntityTracker entry) {
        this(map, getTracker(entry), getTrackingDistance(entry), getD(entry), getE(entry));
    }

    private boolean isTracked(EntityPlayer player) {
        return trackingMap != null ? trackingMap.containsKey(player) : trackedPlayers.contains(player);
    }

    public void updateLastPlayer(EntityPlayer lastUpdatedPlayer) {
        if (lastUpdatedPlayer != null) {
            Bukkit.getPluginManager().callEvent(
                    new NPCLinkToPlayerEvent(((NPCHolder) tracker).getNPC(), lastUpdatedPlayer.getBukkitEntity()));
        }
    }

    @Override
    public void updatePlayer(final EntityPlayer entityplayer) {
        if (entityplayer instanceof EntityHumanNPC)
            return;

        if (!isTracked(entityplayer) && tracker instanceof NPCHolder) {
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
    private static MethodHandle TRACKING_MAP_GETTER;
    private static MethodHandle TRACKING_MAP_SETTER;
    private static final MethodHandle TRACKING_RANGE = NMS.getGetter(EntityTracker.class, "trackingDistance");
    private static final MethodHandle TRACKING_RANGE_SETTER = NMS.getFirstFinalSetter(EntityTracker.class, int.class);
    private static final MethodHandle TRACKING_SET_SETTER = NMS.getFirstFinalSetter(EntityTracker.class, Set.class);
    static {
        try {
            // Old paper versions override the tracked player set to be a map
            if (EntityTracker.class.getField("trackedPlayerMap") != null) {
                TRACKING_MAP_SETTER = NMS.getFirstSetter(EntityTracker.class, Map.class);
                TRACKING_MAP_GETTER = NMS.getFirstGetter(EntityTracker.class, Map.class);
            }
        } catch (Exception e) {
        }
    }
}
