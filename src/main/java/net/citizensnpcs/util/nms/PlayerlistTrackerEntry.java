package net.citizensnpcs.util.nms;

import java.lang.reflect.Field;

import org.bukkit.entity.Player;

import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.EntityTrackerEntry;

public class PlayerlistTrackerEntry extends EntityTrackerEntry {
    public PlayerlistTrackerEntry(Entity entity, int i, int j, boolean flag) {
        super(entity, i, j, flag);
    }

    public PlayerlistTrackerEntry(EntityTrackerEntry entry) {
        this(entry.tracker, entry.b, entry.c, getU(entry));
    }

    @Override
    public void updatePlayer(final EntityPlayer entityplayer) {
        // prevent updates to NPC "viewers"
        if (entityplayer instanceof EntityHumanNPC)
            return;

        if (entityplayer != this.tracker && c(entityplayer)) {

            if (!this.trackedPlayers.contains(entityplayer)
                    && ((entityplayer.u().getPlayerChunkMap().a(entityplayer, this.tracker.ae, this.tracker.ag))
                            || (this.tracker.attachedToPlayer))) {

                if ((this.tracker instanceof SkinnableEntity)) {

                    SkinnableEntity skinnable = (SkinnableEntity) this.tracker;

                    Player player = skinnable.getBukkitEntity();
                    if (!entityplayer.getBukkitEntity().canSee(player))
                        return;

                    skinnable.getSkinTracker().updateViewer(entityplayer.getBukkitEntity());
                }
            }
        }
        super.updatePlayer(entityplayer);
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

    private static Field U = NMS.getField(EntityTrackerEntry.class, "u");
}
