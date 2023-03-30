package net.citizensnpcs.trait;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.flocking.Flocker;
import net.citizensnpcs.api.ai.flocking.RadiusNPCFlock;
import net.citizensnpcs.api.ai.flocking.SeparationBehavior;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Persists a {@link Player} to follow while spawned. Optionally allows protecting of the player as well.
 */
@TraitName("followtrait")
public class FollowTrait extends Trait {
    private Entity entity;
    private Flocker flock;
    @Persist
    private UUID followingUUID;
    @Persist
    private boolean protect;

    public FollowTrait() {
        super("followtrait");
    }

    public Entity getFollowing() {
        return entity;
    }

    /**
     * Returns whether the trait is actively following a {@link Entity}.
     */
    public boolean isActive() {
        return npc.isSpawned() && entity != null;
    }

    public boolean isEnabled() {
        return followingUUID != null;
    }

    @Override
    public void onDespawn() {
        flock = null;
    }

    @EventHandler
    private void onEntityDamage(EntityDamageByEntityEvent event) {
        if (isActive() && protect && event.getEntity().equals(entity)) {
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
    public void onSpawn() {
        flock = new Flocker(npc, new RadiusNPCFlock(4, 0), new SeparationBehavior(1));
    }

    @Override
    public void run() {
        if (entity == null || !entity.isValid()) {
            if (followingUUID == null)
                return;
            entity = Bukkit.getPlayer(followingUUID);
            if (entity == null) {
                entity = Bukkit.getEntity(followingUUID);
            }
            if (entity == null)
                return;
        }

        if (!isActive())
            return;

        if (!npc.getEntity().getWorld().equals(entity.getWorld())) {
            if (Setting.FOLLOW_ACROSS_WORLDS.asBoolean()) {
                npc.teleport(entity.getLocation(), TeleportCause.PLUGIN);
            }
            return;
        }
        if (!npc.getNavigator().isNavigating()) {
            npc.getNavigator().setTarget(entity, false);
        } else {
            flock.run();
        }
    }

    /**
     * Toggles and/or sets the {@link Entity} to follow and whether to protect them (similar to wolves in Minecraft,
     * attack whoever attacks the entity).
     *
     * Will toggle if the {@link Entity} is the entity currently being followed.
     *
     * @param entity
     *            the player to follow
     * @param protect
     *            whether to protect the player
     * @return whether the trait is enabled
     */
    public boolean toggle(Entity entity, boolean protect) {
        this.protect = protect;
        this.followingUUID = entity.getUniqueId().equals(followingUUID) ? null : entity.getUniqueId();
        if (npc.getNavigator().isNavigating() && this.entity != null && npc.getNavigator().getEntityTarget() != null
                && this.entity == npc.getNavigator().getEntityTarget().getTarget()) {
            npc.getNavigator().cancelNavigation();
        }
        this.entity = null;
        return followingUUID != null;
    }
}
