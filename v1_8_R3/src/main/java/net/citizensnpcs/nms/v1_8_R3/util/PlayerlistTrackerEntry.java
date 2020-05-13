package net.citizensnpcs.nms.v1_8_R3.util;

import java.lang.reflect.Field;

import org.bukkit.entity.Player;

import net.citizensnpcs.nms.v1_8_R3.entity.EntityHumanNPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;

public class PlayerlistTrackerEntry extends EntityTrackerEntry {
    public PlayerlistTrackerEntry(Entity entity, int i, int j, boolean flag) {
        super(entity, i, j, flag);
    }

    public PlayerlistTrackerEntry(EntityTrackerEntry entry) {
        this(getTracker(entry), getB(entry), getC(entry), getU(entry));
    }

    @Override
    public void updatePlayer(final EntityPlayer entityplayer) {
        // prevent updates to NPC "viewers"
        if (entityplayer instanceof EntityHumanNPC)
            return;
        Entity tracker = getTracker(this);
        if (entityplayer != tracker && c(entityplayer)) {
            if (!this.trackedPlayers.contains(entityplayer)
                    && ((entityplayer.u().getPlayerChunkMap().a(entityplayer, tracker.ae, tracker.ag))
                            || (tracker.attachedToPlayer))) {
                if ((tracker instanceof SkinnableEntity)) {
                    SkinnableEntity skinnable = (SkinnableEntity) tracker;

                    Player player = skinnable.getBukkitEntity();
                    if (!entityplayer.getBukkitEntity().canSee(player))
                        return;

                    skinnable.getSkinTracker().updateViewer(entityplayer.getBukkitEntity());
                }
            }
        }
        super.updatePlayer(entityplayer);
    }

    private static int getB(EntityTrackerEntry entry) {
        try {
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

    private static Field B = NMS.getField(EntityTrackerEntry.class, "b");
    private static Field C = NMS.getField(EntityTrackerEntry.class, "c");
    private static Field TRACKER = NMS.getField(EntityTrackerEntry.class, "tracker");
    private static Field U = NMS.getField(EntityTrackerEntry.class, "u");
}
