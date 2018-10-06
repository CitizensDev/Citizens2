package net.citizensnpcs.trait;

import org.bukkit.Bukkit;
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
    private String followingName;
    private Player player;
    @Persist("protect")
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
            if (followingName == null)
                return;
            player = Bukkit.getPlayerExact(followingName);
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

    public boolean toggle(String name, boolean protect) {
        this.protect = protect;
        if (name.equalsIgnoreCase(this.followingName) || this.followingName == null) {
            this.active = !active;
        }
        this.followingName = name;
        if (npc.getNavigator().isNavigating() && player != null
                && player == npc.getNavigator().getEntityTarget().getTarget()) {
            npc.getNavigator().cancelNavigation();
        }
        this.player = null;
        return this.active;
    }
}
