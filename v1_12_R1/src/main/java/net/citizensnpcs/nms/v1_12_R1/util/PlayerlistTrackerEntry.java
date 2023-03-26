package net.citizensnpcs.nms.v1_12_R1.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_12_R1.entity.EntityHumanNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EntityTracker;
import net.minecraft.server.v1_12_R1.EntityTrackerEntry;

public class PlayerlistTrackerEntry extends EntityTrackerEntry {
    private EntityPlayer lastUpdatedPlayer;
    private final Entity tracker;

    public PlayerlistTrackerEntry(Entity entity, int i, int j, int k, boolean flag) {
        super(entity, i, j, k, flag);
        this.tracker = getTracker(this);
    }

    public PlayerlistTrackerEntry(EntityTrackerEntry entry) {
        this(getTracker(entry), getE(entry), getF(entry), getG(entry), getU(entry));
    }

    public boolean isUpdating() {
        return lastUpdatedPlayer != null;
    }

    public void updateLastPlayer() {
        if ((lastUpdatedPlayer == null) || tracker.dead || lastUpdatedPlayer == null
                || tracker.getBukkitEntity().getType() != EntityType.PLAYER)
            return;
        final EntityPlayer entityplayer = lastUpdatedPlayer;
        NMS.sendTabListAdd(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
        lastUpdatedPlayer = null;
        if (!Setting.DISABLE_TABLIST.asBoolean())
            return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                NMS.sendTabListRemove(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
            }
        }, Setting.TABLIST_REMOVE_PACKET_DELAY.asTicks());
    }

    @Override
    public void updatePlayer(final EntityPlayer entityplayer) {
        if (tracker instanceof NPCHolder) {
            NPC npc = ((NPCHolder) tracker).getNPC();
            if (npc.isHiddenFrom(entityplayer.getBukkitEntity()))
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

        this.lastUpdatedPlayer = entityplayer;
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
    private static final MethodHandle TRACKING_RANGE_SETTER = NMS.getFirstFinalSetter(EntityTracker.class, int.class);
    private static Field U = NMS.getField(EntityTrackerEntry.class, "u");
}
