package net.citizensnpcs.npc.ai.tree;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.expr.ExpressionScope;
import net.citizensnpcs.api.npc.NPC;

/**
 * Factory for creating expression scopes with NPC-related bindings.
 */
public class NPCExpressionScope {
    /**
     * Creates an expression scope with lazy bindings for NPC properties.
     *
     * @param npc
     *            the NPC to bind
     * @return a scope with NPC bindings
     */
    public static ExpressionScope createFor(NPC npc) {
        ExpressionScope scope = new ExpressionScope();

        scope.bind("npc.id", npc::getId);
        scope.bind("npc.name", npc::getFullName);
        scope.bind("npc.uuid", () -> npc.getUniqueId().toString());
        scope.bind("npc.spawned", npc::isSpawned);
        scope.bind("npc.protected", npc::isProtected);
        scope.bind("npc.flyable", npc::isFlyable);

        scope.bind("npc.x", () -> {
            Location loc = npc.getStoredLocation();
            return loc != null ? loc.getX() : 0;
        });
        scope.bind("npc.y", () -> {
            Location loc = npc.getStoredLocation();
            return loc != null ? loc.getY() : 0;
        });
        scope.bind("npc.z", () -> {
            Location loc = npc.getStoredLocation();
            return loc != null ? loc.getZ() : 0;
        });
        scope.bind("npc.yaw", () -> {
            Location loc = npc.getStoredLocation();
            return loc != null ? loc.getYaw() : 0;
        });
        scope.bind("npc.pitch", () -> {
            Location loc = npc.getStoredLocation();
            return loc != null ? loc.getPitch() : 0;
        });

        scope.bind("npc.health", () -> {
            Entity entity = npc.getEntity();
            if (entity instanceof LivingEntity) {
                return ((LivingEntity) entity).getHealth();
            }
            return 0;
        });
        scope.bind("npc.maxhealth", () -> {
            Entity entity = npc.getEntity();
            if (entity instanceof LivingEntity) {
                return ((LivingEntity) entity).getMaxHealth();
            }
            return 0;
        });
        scope.bind("npc.velocity.x", () -> {
            Entity entity = npc.getEntity();
            return entity != null ? entity.getVelocity().getX() : 0;
        });
        scope.bind("npc.velocity.y", () -> {
            Entity entity = npc.getEntity();
            return entity != null ? entity.getVelocity().getY() : 0;
        });
        scope.bind("npc.velocity.z", () -> {
            Entity entity = npc.getEntity();
            return entity != null ? entity.getVelocity().getZ() : 0;
        });

        // Target properties
        scope.bind("target.exists", () -> {
            EntityTarget target = npc.getNavigator().getEntityTarget();
            return target != null && target.getTarget() != null && target.getTarget().isValid();
        });
        scope.bind("target.x", () -> {
            EntityTarget target = npc.getNavigator().getEntityTarget();
            if (target != null && target.getTarget() != null) {
                return target.getTarget().getLocation().getX();
            }
            return 0;
        });
        scope.bind("target.y", () -> {
            EntityTarget target = npc.getNavigator().getEntityTarget();
            if (target != null && target.getTarget() != null) {
                return target.getTarget().getLocation().getY();
            }
            return 0;
        });
        scope.bind("target.z", () -> {
            EntityTarget target = npc.getNavigator().getEntityTarget();
            if (target != null && target.getTarget() != null) {
                return target.getTarget().getLocation().getZ();
            }
            return 0;
        });
        scope.bind("target.distance", () -> {
            EntityTarget target = npc.getNavigator().getEntityTarget();
            if (target != null && target.getTarget() != null) {
                Location npcLoc = npc.getStoredLocation();
                if (npcLoc != null) {
                    return npcLoc.distance(target.getTarget().getLocation());
                }
            }
            return Double.MAX_VALUE;
        });
        scope.bind("target.health", () -> {
            EntityTarget target = npc.getNavigator().getEntityTarget();
            if (target != null && target.getTarget() instanceof LivingEntity) {
                return ((LivingEntity) target.getTarget()).getHealth();
            }
            return 0;
        });

        // Navigator state
        scope.bind("nav.navigating", npc.getNavigator()::isNavigating);
        scope.bind("nav.paused", npc.getNavigator()::isPaused);

        // Nearby entity count (expensive - use sparingly)
        scope.bind("nearby.count", () -> {
            if (npc.isSpawned())
                return npc.getEntity().getNearbyEntities(10, 10, 10).size();

            return 0;
        });

        // Nearby player detection
        scope.bind("nearby.player", () -> CitizensAPI.getLocationLookup().getNearbyPlayers(npc.getStoredLocation(), 20)
                .iterator().hasNext());

        // Distance to nearest player
        scope.bind("nearby.player.distance", () -> {
            Location stored = npc.getStoredLocation();
            double nearest = Double.MAX_VALUE;
            for (Entity nearby : CitizensAPI.getLocationLookup().getNearbyPlayers(stored, 20)) {
                double dist = stored.distance(nearby.getLocation());
                if (dist < nearest) {
                    nearest = dist;
                }
            }
            return nearest < Double.MAX_VALUE ? nearest : 0;
        });

        return scope;
    }
}
