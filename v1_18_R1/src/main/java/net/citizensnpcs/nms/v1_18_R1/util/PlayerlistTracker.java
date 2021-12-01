package net.citizensnpcs.nms.v1_18_R1.util;

import java.lang.invoke.MethodHandle;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.nms.v1_18_R1.entity.EntityHumanNPC;
import net.citizensnpcs.util.NMS;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkMap.TrackedEntity;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class PlayerlistTracker extends ChunkMap.TrackedEntity {
    private ServerPlayer lastUpdatedPlayer;
    private final Entity tracker;

    public PlayerlistTracker(ChunkMap map, Entity entity, int i, int j, boolean flag) {
        map.super(entity, i, j, flag);
        this.tracker = entity;
    }

    public PlayerlistTracker(ChunkMap map, TrackedEntity entry) {
        this(map, getTracker(entry), getTrackingDistance(entry), getE(entry), getF(entry));
    }

    public void updateLastPlayer() {
        if (tracker.isRemoved())
            return;
        final ServerPlayer entityplayer = lastUpdatedPlayer;
        if (entityplayer == null)
            return;
        NMS.sendTabListAdd(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
        if (!Setting.DISABLE_TABLIST.asBoolean())
            return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
            @Override
            public void run() {
                NMSImpl.sendPacket(entityplayer.getBukkitEntity(), new ClientboundAnimatePacket(tracker, 0));
                NMS.sendTabListRemove(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
            }
        }, Setting.TABLIST_REMOVE_PACKET_DELAY.asInt());
    }

    @Override
    public void updatePlayer(final ServerPlayer entityplayer) {
        if (entityplayer instanceof EntityHumanNPC) // prevent updates to NPC "viewers"
            return;
        this.lastUpdatedPlayer = entityplayer;
        super.updatePlayer(entityplayer);

    }

    private static int getE(TrackedEntity entry) {
        try {
            return (int) E.invoke(TRACKER_ENTRY.invoke(entry));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static boolean getF(TrackedEntity entry) {
        try {
            return (boolean) F.invoke(TRACKER_ENTRY.invoke(entry));
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
            return (Integer) TRACKING_DISTANCE.invoke(entry);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static final MethodHandle E = NMS.getGetter(ServerEntity.class, "e");
    private static final MethodHandle F = NMS.getGetter(ServerEntity.class, "f");
    private static final MethodHandle TRACKER = NMS.getFirstGetter(TrackedEntity.class, Entity.class);
    private static final MethodHandle TRACKER_ENTRY = NMS.getFirstGetter(TrackedEntity.class, ServerEntity.class);
    private static final MethodHandle TRACKING_DISTANCE = NMS.getFirstGetter(TrackedEntity.class, int.class);
}
