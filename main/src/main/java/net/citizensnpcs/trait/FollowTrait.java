package net.citizensnpcs.trait;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Persists a {@link Player} to follow while spawned. Optionally allows protecting of the player as well.
 */
@TraitName("followtrait")
public class FollowTrait extends Trait {
    @Persist("active")
    private boolean enabled = false;
    @Persist
    private UUID followingUUID;
    private Player player;
    @Persist
    private boolean protect;

    public FollowTrait() {
        super("followtrait");
    }

    public Player getFollowingPlayer() {
        return player;
    }

    /**
     * Returns whether the trait is actively following a {@link Player}.
     */
    public boolean isActive() {
        return enabled && npc.isSpawned() && player != null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler
    private void onEntityDamage(EntityDamageByEntityEvent event) {
        if (isActive() && protect && event.getEntity().equals(player)) {
            Entity damager = event.getDamager();
            if (event.getEntity() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getEntity();
                if (projectile.getShooter() instanceof Entity) {
                    damager = (Entity) projectile.getShooter();
                }
            }
            npc.getNavigator().setTarget(damager, true);
        }
    }

    @Override
    public void run() {
        if (player == null || !player.isValid()) {
            if (followingUUID == null)
                return;
            player = Bukkit.getPlayer(followingUUID);
            if (player == null) {
                return;
            }
        }
        if (!isActive()) {
            return;
        }
        if (!npc.getEntity().getWorld().equals(player.getWorld())) {
            if (Setting.FOLLOW_ACROSS_WORLDS.asBoolean()) {
                npc.teleport(player.getLocation(), TeleportCause.PLUGIN);
            }
            return;
        }
        if (!npc.getNavigator().isNavigating()) {
            npc.getNavigator().setTarget(player, false);
        }
    }

    /**
     * Toggles and/or sets the {@link OfflinePlayer} to follow and whether to protect them (similar to wolves in
     * Minecraft, attack whoever attacks the player).
     *
     * Will toggle if the {@link OfflinePlayer} is the player currently being followed.
     *
     * @param player
     *            the player to follow
     * @param protect
     *            whether to protect the player
     * @return whether the trait is enabled
     */
    public boolean toggle(OfflinePlayer player, boolean protect) {
        this.protect = protect;
        if (player.getUniqueId().equals(this.followingUUID) || this.followingUUID == null) {
            this.enabled = !enabled;
        }
        this.followingUUID = player.getUniqueId();
        if (npc.getNavigator().isNavigating() && this.player != null && npc.getNavigator().getEntityTarget() != null
                && this.player == npc.getNavigator().getEntityTarget().getTarget()) {
            npc.getNavigator().cancelNavigation();
        }
        this.player = null;
        return this.enabled;
    }
}
