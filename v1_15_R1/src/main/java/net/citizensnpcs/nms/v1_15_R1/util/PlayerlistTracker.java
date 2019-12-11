package net.citizensnpcs.nms.v1_15_R1.util;

import java.lang.invoke.MethodHandle;

import org.bukkit.entity.Player;

import net.citizensnpcs.nms.v1_15_R1.entity.EntityHumanNPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_15_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EntityTrackerEntry;
import net.minecraft.server.v1_15_R1.PlayerChunk;
import net.minecraft.server.v1_15_R1.PlayerChunkMap;
import net.minecraft.server.v1_15_R1.PlayerChunkMap.EntityTracker;
import net.minecraft.server.v1_15_R1.Vec3D;

public class PlayerlistTracker extends PlayerChunkMap.EntityTracker {
    private final PlayerChunkMap map;
    private final Entity tracker;
    private final EntityTrackerEntry trackerEntry;
    private final int trackingDistance;

    public PlayerlistTracker(PlayerChunkMap map, Entity entity, int i, int j, boolean flag) {
        map.super(entity, i, j, flag);
        this.map = map;
        this.tracker = getTracker(this);
        this.trackerEntry = getTrackerEntry(this);
        this.trackingDistance = i;
    }

    public PlayerlistTracker(PlayerChunkMap map, EntityTracker entry) {
        this(map, getTracker(entry), getTrackingDistance(entry), getD(entry), getE(entry));
    }

    private int getb(ChunkCoordIntPair chunkcoordintpair, EntityPlayer entityplayer, boolean b) {
        try {
            return (int) B.invoke(chunkcoordintpair, entityplayer, b);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getViewDistance(PlayerChunkMap map2) {
        try {
            return (int) VIEW_DISTANCE.invoke(map2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private PlayerChunk getVisibleChunk(long pair) {
        try {
            return (PlayerChunk) GET_VISIBLE_CHUNK.invoke(map, pair);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updatePlayer(final EntityPlayer entityplayer) {
        if (entityplayer instanceof EntityHumanNPC)
            return; // prevent updates to NPC "viewers"
        Entity tracker = getTracker(this);
        final Vec3D vec3d = new Vec3D(entityplayer.locX(), entityplayer.locY(), entityplayer.locZ())
                .d(this.trackerEntry.b());
        final int i = Math.min(this.trackingDistance, (getViewDistance(map) - 1) * 16);
        final boolean flag = vec3d.x >= -i && vec3d.x <= i && vec3d.z >= -i && vec3d.z <= i
                && this.tracker.a(entityplayer);
        if (entityplayer != tracker && flag && tracker instanceof SkinnableEntity) {
            /* boolean flag1 = this.tracker.attachedToPlayer;
            if (!flag1) {
                ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(this.tracker.chunkX, this.tracker.chunkZ);
                PlayerChunk playerchunk = getVisibleChunk(chunkcoordintpair.pair());
                if (playerchunk.getChunk() != null) {
                    flag1 = getb(chunkcoordintpair, entityplayer, false) <= getViewDistance(map);
                }
            }*/
            if (!this.trackedPlayers.contains(entityplayer)) {
                SkinnableEntity skinnable = (SkinnableEntity) tracker;

                Player player = skinnable.getBukkitEntity();
                if (!entityplayer.getBukkitEntity().canSee(player))
                    return;

                skinnable.getSkinTracker().updateViewer(entityplayer.getBukkitEntity());
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

    private static int getTrackingDistance(EntityTracker entry) {
        try {
            return (Integer) TRACKING_DISTANCE.invoke(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static Entity getTracker(EntityTracker entry) {
        try {
            return (Entity) TRACKER.invoke(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static EntityTrackerEntry getTrackerEntry(EntityTracker entry) {
        try {
            return (EntityTrackerEntry) TRACKER_ENTRY.invoke(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final MethodHandle B = NMS.getMethodHandle(PlayerChunkMap.class, "b", true, ChunkCoordIntPair.class,
            EntityPlayer.class, boolean.class);
    private static final MethodHandle D = NMS.getGetter(EntityTrackerEntry.class, "d");
    private static final MethodHandle E = NMS.getGetter(EntityTrackerEntry.class, "e");
    private static final MethodHandle GET_VISIBLE_CHUNK = NMS.getMethodHandle(PlayerChunkMap.class, "getVisibleChunk",
            true, long.class);
    private static final MethodHandle TRACKING_DISTANCE = NMS.getGetter(EntityTracker.class, "trackingDistance");
    private static final MethodHandle TRACKER = NMS.getGetter(EntityTracker.class, "tracker");
    private static final MethodHandle TRACKER_ENTRY = NMS.getGetter(EntityTracker.class, "trackerEntry");
    private static final MethodHandle VIEW_DISTANCE = NMS.getGetter(PlayerChunkMap.class, "viewDistance");
}
