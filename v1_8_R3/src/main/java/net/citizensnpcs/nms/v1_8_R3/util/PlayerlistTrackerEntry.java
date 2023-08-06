package net.citizensnpcs.nms.v1_8_R3.util;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCSeenByPlayerEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_8_R3.entity.EntityHumanNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;

public class PlayerlistTrackerEntry extends EntityTrackerEntry {
    private EntityPlayer lastUpdatedPlayer;

    public PlayerlistTrackerEntry(Entity entity, int i, int j, boolean flag) {
        super(entity, i, j, flag);
    }

    public PlayerlistTrackerEntry(EntityTrackerEntry entry) {
        this(entry.tracker, getB(entry), getC(entry), getU(entry));
    }

    public boolean isUpdating() {
        return lastUpdatedPlayer != null;
    }

    public void updateLastPlayer() {
        if ((lastUpdatedPlayer == null) || tracker.dead || tracker.getBukkitEntity().getType() != EntityType.PLAYER)
            return;
        final EntityPlayer entityplayer = lastUpdatedPlayer;
        NMS.sendTabListAdd(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
        lastUpdatedPlayer = null;
        NPC npc = ((NPCHolder) tracker).getNPC();
        if (npc.data().get(NPC.Metadata.RESET_YAW_ON_SPAWN, Setting.RESET_YAW_ON_SPAWN.asBoolean())) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(),
                    () -> NMSImpl.sendPacket(entityplayer.getBukkitEntity(), new PacketPlayOutAnimation(tracker, 0)),
                    1);
        }
        if (!Setting.DISABLE_TABLIST.asBoolean())
            return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
            NMS.sendTabListRemove(entityplayer.getBukkitEntity(), (Player) tracker.getBukkitEntity());
        }, Setting.TABLIST_REMOVE_PACKET_DELAY.asTicks());
    }

    @Override
    public void updatePlayer(final EntityPlayer entityplayer) {
        // prevent updates to NPC "viewers"
        if (entityplayer instanceof EntityHumanNPC)
            return;
        if (!trackedPlayers.contains(entityplayer) && tracker instanceof NPCHolder) {
            NPC npc = ((NPCHolder) tracker).getNPC();
            NPCSeenByPlayerEvent event = new NPCSeenByPlayerEvent(npc, entityplayer.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                return;
        }
        lastUpdatedPlayer = entityplayer;
        super.updatePlayer(entityplayer);
        lastUpdatedPlayer = null;
    }

    private static int getB(EntityTrackerEntry entry) {
        try {
            Entity entity = entry.tracker;
            if (entity instanceof NPCHolder) {
                return ((NPCHolder) entity).getNPC().data().get(NPC.Metadata.TRACKING_RANGE, (Integer) B.get(entry));
            }
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
    private static Field U = NMS.getField(EntityTrackerEntry.class, "u");
}
