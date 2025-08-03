package net.citizensnpcs.nms.v1_8_R3.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingSet;

import net.citizensnpcs.api.event.NPCLinkToPlayerEvent;
import net.citizensnpcs.api.event.NPCSeenByPlayerEvent;
import net.citizensnpcs.api.event.NPCUnlinkFromPlayerEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_8_R3.entity.EntityHumanNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;

public class PlayerlistTrackerEntry extends EntityTrackerEntry {
    private Map<EntityPlayer, Boolean> trackingMap;

    public PlayerlistTrackerEntry(Entity entity, int i, int j, boolean flag) {
        super(entity, i, j, flag);
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

                    @Override
                    public Boolean remove(Object conn) {
                        Boolean removed = super.remove(conn);
                        if (removed) {
                            Bukkit.getPluginManager().callEvent(new NPCUnlinkFromPlayerEvent(
                                    ((NPCHolder) tracker).getNPC(), ((EntityPlayer) conn).getBukkitEntity()));
                        }
                        return removed;
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

                    @Override
                    public boolean remove(Object conn) {
                        boolean removed = super.remove(conn);
                        if (removed) {
                            Bukkit.getPluginManager().callEvent(new NPCUnlinkFromPlayerEvent(
                                    ((NPCHolder) tracker).getNPC(), ((EntityPlayer) conn).getBukkitEntity()));
                        }
                        return removed;
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public PlayerlistTrackerEntry(EntityTrackerEntry entry) {
        this(entry.tracker, getB(entry), getC(entry), getU(entry));
    }

    private boolean isTracked(EntityPlayer player) {
        return trackingMap != null ? trackingMap.containsKey(player) : trackedPlayers.contains(player);
    }

    public void updateLastPlayer(EntityPlayer lastUpdatedPlayer) {
        if (lastUpdatedPlayer != null) {
            Bukkit.getPluginManager().callEvent(new NPCLinkToPlayerEvent(((NPCHolder) tracker).getNPC(),
                    lastUpdatedPlayer.getBukkitEntity(), !Bukkit.isPrimaryThread()));
            lastUpdatedPlayer = null;
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
            Integer trackingRange = npc.data().get(NPC.Metadata.TRACKING_RANGE);
            if (trackingRange != null && npc.data().get("last-tracking-range", -1) != b) {
                b = trackingRange;
                npc.data().set("last-tracking-range", trackingRange);
            }
        }
        super.updatePlayer(entityplayer);
    }

    private static int getB(EntityTrackerEntry entry) {
        try {
            Entity entity = entry.tracker;
            if (entity instanceof NPCHolder)
                return ((NPCHolder) entity).getNPC().data().get(NPC.Metadata.TRACKING_RANGE, (Integer) B.get(entry));
            return (Integer) B.get(entry);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int getC(EntityTrackerEntry entry) {
        try {
            return (Integer) C.get(entry);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Set<org.bukkit.entity.Player> getSeenBy(EntityTrackerEntry tracker) {
        if (TRACKING_MAP_GETTER != null) {
            Map<EntityPlayer, Boolean> delegate;
            try {
                delegate = (Map<EntityPlayer, Boolean>) TRACKING_MAP_GETTER.invoke(tracker);
            } catch (Throwable e) {
                return null;
            }
            return delegate.keySet().stream()
                    .map((Function<? super EntityPlayer, ? extends CraftPlayer>) EntityPlayer::getBukkitEntity)
                    .collect(Collectors.toSet());
        } else
            return tracker.trackedPlayers.stream()
                    .map((Function<? super EntityPlayer, ? extends CraftPlayer>) EntityPlayer::getBukkitEntity)
                    .collect(Collectors.toSet());
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

    private static Field B = NMS.getField(EntityTrackerEntry.class, "b");
    private static Field C = NMS.getField(EntityTrackerEntry.class, "c");
    private static MethodHandle TRACKING_MAP_GETTER;
    private static MethodHandle TRACKING_MAP_SETTER;
    private static final MethodHandle TRACKING_SET_SETTER = NMS.getFirstFinalSetter(EntityTrackerEntry.class,
            Set.class);
    private static Field U = NMS.getField(EntityTrackerEntry.class, "u");
    static {
        try {
            // Old paper versions override the tracked player set to be a map
            if (EntityTrackerEntry.class.getField("trackedPlayerMap") != null) {
                TRACKING_MAP_SETTER = NMS.getFirstSetter(EntityTrackerEntry.class, Map.class);
                TRACKING_MAP_GETTER = NMS.getFirstGetter(EntityTrackerEntry.class, Map.class);
            }
        } catch (Exception e) {
        }
    }
}
