package net.citizensnpcs.npc.skin;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.util.NMS;

/**
 * Handles and synchronizes add and remove packets for Player type NPC's in order to properly apply the NPC skin.
 *
 * <p>
 * Used as one instance per NPC entity.
 * </p>
 */
public class SkinPacketTracker {
    private final SkinnableEntity entity;
    private boolean isRemoved;
    private Skin skin;

    /**
     * Constructor.
     *
     * @param entity
     *            The skinnable entity the instance belongs to.
     */
    public SkinPacketTracker(SkinnableEntity entity) {
        Objects.requireNonNull(entity);

        this.entity = entity;
        skin = Skin.get(entity);
    }

    /**
     * Get the NPC skin.
     */
    public Skin getSkin() {
        return skin;
    }

    /**
     * Notify that the NPC skin has been changed.
     */
    public void notifySkinChange(boolean forceUpdate) {
        skin = Skin.get(entity, forceUpdate);
        skin.applyAndRespawn(entity);
    }

    /**
     * Send skin related packets to a player.
     *
     * @param player
     *            The player.
     */
    public void updateViewer(Player player) {
        Objects.requireNonNull(player);

        if (isRemoved || player.hasMetadata("NPC"))
            return;

        skin.apply(entity);
        if (entity.getBukkitEntity() instanceof Player && NMS.sendTabListAdd(player, (Player) entity.getBukkitEntity())
                && entity.getNPC().shouldRemoveFromTabList()) {
            CitizensAPI.getScheduler().runEntityTaskLater(player,
                    () -> NMS.sendTabListRemove(player, (Player) entity.getBukkitEntity()),
                    Setting.TABLIST_REMOVE_PACKET_DELAY.asTicks());
        }
    }
}
