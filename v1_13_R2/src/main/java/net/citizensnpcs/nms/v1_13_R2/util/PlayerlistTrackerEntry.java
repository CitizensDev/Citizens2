package net.citizensnpcs.nms.v1_13_R2.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.collect.ForwardingSet;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCSeenByPlayerEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_13_R2.entity.EntityHumanNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.EntityTrackerEntry;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntity.PacketPlayOutEntityLook;

public class PlayerlistTrackerEntry extends EntityTrackerEntry {
    private final Entity tracker;

    public PlayerlistTrackerEntry(Entity entity, int i, int j, int k, boolean flag) {
        super(entity, i, j, k, flag);
        tracker = getTracker(this);
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

    public PlayerlistTrackerEntry(EntityTrackerEntry entry) {
        this(getTracker(entry), getE(entry), getF(entry), getG(entry), getU(entry));
    }

    public void updateLastPlayer(EntityPlayer lastUpdatedPlayer) {
        if (tracker.dead || lastUpdatedPlayer == null || tracker.getBukkitEntity().getType() != EntityType.PLAYER)
            return;
        final EntityPlayer entityplayer = lastUpdatedPlayer;
        NMS.sendTabListAdd(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
        lastUpdatedPlayer = null;
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

    private static int getE(EntityTrackerEntry entry) {
        try {
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
    private static final MethodHandle TRACKING_RANGE_SETTER = NMS.getFirstFinalSetter(EntityTrackerEntry.class,
            int.class);
    private static final MethodHandle TRACKING_SET_SETTER = NMS.getFirstFinalSetter(EntityTrackerEntry.class,
            Set.class);
    private static Field U = NMS.getField(EntityTrackerEntry.class, "u");
}
