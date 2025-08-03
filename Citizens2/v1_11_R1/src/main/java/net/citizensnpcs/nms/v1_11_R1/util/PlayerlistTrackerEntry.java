package net.citizensnpcs.nms.v1_11_R1.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ForwardingSet;

import net.citizensnpcs.api.event.NPCLinkToPlayerEvent;
import net.citizensnpcs.api.event.NPCSeenByPlayerEvent;
import net.citizensnpcs.api.event.NPCUnlinkFromPlayerEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_11_R1.Entity;
import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.EntityTrackerEntry;

public class PlayerlistTrackerEntry extends EntityTrackerEntry {
    private Entity tracker;
    private Map<EntityPlayer, Boolean> trackingMap;

    public PlayerlistTrackerEntry(Entity entity, int i, int j, int k, boolean flag) {
        super(entity, i, j, k, flag);
        tracker = getTracker(this);
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
        this(getTracker(entry), getE(entry), getF(entry), getG(entry), getU(entry));
    }

    private boolean isTracked(EntityPlayer player) {
        return trackingMap != null ? trackingMap.containsKey(player) : trackedPlayers.contains(player);
    }

    public void updateLastPlayer(EntityPlayer lastUpdatedPlayer) {
        if (lastUpdatedPlayer != null) {
            Bukkit.getPluginManager().callEvent(new NPCLinkToPlayerEvent(((NPCHolder) tracker).getNPC(),
                    lastUpdatedPlayer.getBukkitEntity(), !Bukkit.isPrimaryThread()));
        }
    }

    @Override
    public void updatePlayer(final EntityPlayer entityplayer) {
        if (entityplayer instanceof NPCHolder)
            return;

        if (!isTracked(entityplayer) && tracker instanceof NPCHolder) {
            NPC npc = ((NPCHolder) tracker).getNPC();
            NPCSeenByPlayerEvent event = new NPCSeenByPlayerEvent(npc, entityplayer.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                return;
        }
        super.updatePlayer(entityplayer);
    }

    private static int getE(EntityTrackerEntry entry) {
        try {
            Entity entity = getTracker(entry);
            if (entity instanceof NPCHolder)
                return ((NPCHolder) entity).getNPC().data().get(NPC.Metadata.TRACKING_RANGE, (Integer) E.get(entry));
            return (Integer) E.get(entry);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int getF(EntityTrackerEntry entry) {
        try {
            return (Integer) F.get(entry);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int getG(EntityTrackerEntry entry) {
        try {
            return (Integer) G.get(entry);
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

    private static Entity getTracker(EntityTrackerEntry entry) {
        try {
            return (Entity) TRACKER.get(entry);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
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

    private static Field E = NMS.getField(EntityTrackerEntry.class, "e");

    private static Field F = NMS.getField(EntityTrackerEntry.class, "f");

    private static Field G = NMS.getField(EntityTrackerEntry.class, "g");

    private static Field TRACKER = NMS.getField(EntityTrackerEntry.class, "tracker");
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
