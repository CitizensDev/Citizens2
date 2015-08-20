package net.citizensnpcs.util.nms;

import net.citizensnpcs.Settings;
import net.citizensnpcs.npc.entity.EntityHumanNPC;
import net.citizensnpcs.npc.entity.EntityHumanPacketTracker;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

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

                if ((this.tracker instanceof EntityHumanNPC)) {

                    EntityHumanNPC humanNPC = (EntityHumanNPC)this.tracker;

                    Player player = humanNPC.getBukkitEntity();
                    if (!entityplayer.getBukkitEntity().canSee(player))
                        return;

                    humanNPC.packetTracker.addViewer(entityplayer);
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
