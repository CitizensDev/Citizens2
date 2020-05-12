package net.citizensnpcs.nms.v1_15_R1.util;

import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EntityTrackerEntry;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import net.minecraft.server.v1_15_R1.WorldServer;

public class TablistRemovingTrackerEntry extends EntityTrackerEntry {
    private final Entity tracker;

    public TablistRemovingTrackerEntry(WorldServer worldserver, Entity entity, int i, boolean flag,
            Consumer<Packet<?>> consumer, Set<EntityPlayer> trackedPlayers) {
        super(worldserver, entity, i, flag, consumer, trackedPlayers);
        this.tracker = entity;
    }

    @Override
    public void b(EntityPlayer entityplayer) {
        PlayerConnection playerconnection = entityplayer.playerConnection;
        if (!tracker.dead) {
            NMS.sendTabListAdd(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
        }
        this.a(playerconnection::sendPacket, entityplayer);
        this.tracker.b(entityplayer);
        entityplayer.d(this.tracker);
        if (!tracker.dead && Setting.DISABLE_TABLIST.asBoolean()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    NMS.sendTabListRemove(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
                }
            });
        }
    }
}
