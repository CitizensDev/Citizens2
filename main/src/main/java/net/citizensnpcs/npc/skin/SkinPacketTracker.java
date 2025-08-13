package net.citizensnpcs.npc.skin;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
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
     * Invoke when the NPC entity is spawned.
     */
    public void onSpawnNPC() {
        isRemoved = false;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.getNPC().isSpawned())
                    return;

                updateNearbyViewers(entity.getNPC().data().get(NPC.Metadata.TRACKING_RANGE,
                        Setting.NPC_SKIN_VIEW_DISTANCE.asInt()));
            }
        }.runTaskLater(CitizensAPI.getPlugin(), 10);
    }

    /**
     * Send skin related packets to all nearby players within the specified block radius.
     *
     * @param radius
     *            The radius.
     */
    public void updateNearbyViewers(double radius) {
        Player from = entity.getBukkitEntity();

        CitizensAPI.getLocationLookup().getNearbyPlayers(from.getLocation(), radius).forEach(player -> {
            if (!player.canSee(from) || player.hasMetadata("NPC"))
                return;
            updateViewer(player);
        });
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
        if (NMS.sendTabListAdd(player, entity.getBukkitEntity()) && entity.getNPC().shouldRemoveFromTabList()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(),
                    () -> NMS.sendTabListRemove(player, entity.getBukkitEntity()),
                    Setting.TABLIST_REMOVE_PACKET_DELAY.asTicks());
        }
    }
}
