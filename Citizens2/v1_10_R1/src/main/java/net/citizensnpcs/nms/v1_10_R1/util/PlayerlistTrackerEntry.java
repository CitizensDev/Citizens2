package net.citizensnpcs.nms.v1_10_R1.util;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.event.NPCLinkToPlayerEvent;
import net.citizensnpcs.api.event.NPCSeenByPlayerEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_10_R1.entity.EntityHumanNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_10_R1.Entity;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.EntityTrackerEntry;

public class PlayerlistTrackerEntry extends EntityTrackerEntry {
    public PlayerlistTrackerEntry(Entity entity, int i, int j, int k, boolean flag) {
        super(entity, i, j, k, flag);
    }

    public PlayerlistTrackerEntry(EntityTrackerEntry entry) {
        this(getTracker(entry), getE(entry), getF(entry), getG(entry), getU(entry));
    }

    @Override
    public void updatePlayer(final EntityPlayer entityplayer) {
        if (entityplayer instanceof EntityHumanNPC)
            return;
        Entity tracker = getTracker(this);
        if (!trackedPlayers.contains(entityplayer) && tracker instanceof NPCHolder) {
            NPC npc = ((NPCHolder) tracker).getNPC();
            NPCSeenByPlayerEvent event = new NPCSeenByPlayerEvent(npc, entityplayer.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                return;
        }
        super.updatePlayer(entityplayer);

        if (tracker.getBukkitEntity().getType() != EntityType.PLAYER)
            return;

        if (entityplayer != tracker && c(entityplayer)) {
            if (!this.trackedPlayers.contains(entityplayer)
                    && (entityplayer.x().getPlayerChunkMap().a(entityplayer, tracker.ac, tracker.ae)
                            || tracker.attachedToPlayer)) {
                if (tracker instanceof SkinnableEntity) {
                    SkinnableEntity skinnable = (SkinnableEntity) tracker;
                    Player player = skinnable.getBukkitEntity();
                    if (!entityplayer.getBukkitEntity().canSee(player))
                        return;
                    skinnable.getSkinTracker().updateViewer(entityplayer.getBukkitEntity());
                }
                Bukkit.getPluginManager().callEvent(new NPCLinkToPlayerEvent(((NPCHolder) tracker).getNPC(),
                        entityplayer.getBukkitEntity(), !Bukkit.isPrimaryThread()));
            }
        }
    }

    private static int getE(EntityTrackerEntry entry) {
        try {
            Entity entity = getTracker(entry);
            if (entity instanceof NPCHolder)
                return ((NPCHolder) entity).getNPC().data().get(NPC.Metadata.TRACKING_RANGE, (Integer) E.get(entry));
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

    public static Set<org.bukkit.entity.Player> getSeenBy(EntityTrackerEntry tracker) {
        return tracker.trackedPlayers.stream()
                .map((Function<? super EntityPlayer, ? extends CraftPlayer>) EntityPlayer::getBukkitEntity)
                .collect(Collectors.toSet());
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
    private static Field U = NMS.getField(EntityTrackerEntry.class, "u");
}
