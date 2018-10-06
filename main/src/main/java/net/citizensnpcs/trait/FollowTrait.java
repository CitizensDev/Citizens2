package net.citizensnpcs.trait;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("followtrait")
public class FollowTrait extends Trait {
    @Persist
    private boolean active = false;
    @Persist
    private UUID followingUUID;
    private Player player;
    @Persist
    private boolean protect;

    public FollowTrait() {
        super("followtrait");
    }

    private boolean isActive() {
        return active && npc.isSpawned() && player != null && npc.getEntity().getWorld().equals(player.getWorld());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (isActive() && event.getEntity().equals(player)) {
            npc.getNavigator().setTarget(event.getDamager(), true);
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
        if (!npc.getNavigator().isNavigating()) {
            npc.getNavigator().setTarget(player, false);
        }
    }

    public boolean toggle(OfflinePlayer player, boolean protect) {
        this.protect = protect;
        if (player.getUniqueId().equals(this.followingUUID) || this.followingUUID == null) {
            this.active = !active;
        }
        this.followingUUID = player.getUniqueId();
        if (npc.getNavigator().isNavigating() && this.player != null
                && this.player == npc.getNavigator().getEntityTarget().getTarget()) {
            npc.getNavigator().cancelNavigation();
        }
        this.player = null;
        return this.active;
    }
}
