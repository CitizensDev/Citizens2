package net.citizensnpcs.nms.v1_14_R1.util;

import java.lang.invoke.MethodHandle;

import net.citizensnpcs.nms.v1_14_R1.entity.EntityHumanNPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTrackerEntry;
import net.minecraft.server.v1_14_R1.PlayerChunkMap;
import net.minecraft.server.v1_14_R1.PlayerChunkMap.EntityTracker;

public class PlayerlistTracker extends PlayerChunkMap.EntityTracker {
    public PlayerlistTracker(PlayerChunkMap map, Entity entity, int i, int j, boolean flag) {
        map.super(entity, i, j, flag);
        setTrackerEntry(new TablistRemovingTrackerEntry(map.world, entity, j, flag, this::broadcast, trackedPlayers));
    }

    public PlayerlistTracker(PlayerChunkMap map, EntityTracker entry) {
        this(map, getTracker(entry), getTrackingDistance(entry), getD(entry), getE(entry));
    }

    private void setTrackerEntry(EntityTrackerEntry entry) {
        try {
            TRACKER_ENTRY_SETTER.invoke(this, entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updatePlayer(final EntityPlayer entityplayer) {
        if (!(entityplayer instanceof EntityHumanNPC)) {
            // prevent updates to NPC "viewers"
            super.updatePlayer(entityplayer);
        }
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
            return (Integer) TRACKING_DISTANCE.invoke(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static final MethodHandle D = NMS.getGetter(EntityTrackerEntry.class, "d");
    private static final MethodHandle E = NMS.getGetter(EntityTrackerEntry.class, "e");
    private static final MethodHandle TRACKER = NMS.getGetter(EntityTracker.class, "tracker");
    private static final MethodHandle TRACKER_ENTRY = NMS.getGetter(EntityTracker.class, "trackerEntry");
    private static final MethodHandle TRACKER_ENTRY_SETTER = NMS.getSetter(EntityTracker.class, "trackerEntry");
    private static final MethodHandle TRACKING_DISTANCE = NMS.getGetter(EntityTracker.class, "trackingDistance");
}
