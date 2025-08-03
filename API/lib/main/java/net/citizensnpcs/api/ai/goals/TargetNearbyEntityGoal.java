package net.citizensnpcs.api.ai.goals;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;

/**
 * A sample {@link Goal}/{@link Behavior} that will target specific {@link EntityType}s within a certain radius and
 * start following them using {@link Navigator#setTarget(Entity, boolean)}.
 */
public class TargetNearbyEntityGoal extends BehaviorGoalAdapter {
    private final boolean aggressive;
    private final Function<Entity, Boolean> filter;
    private boolean finished;
    private final NPC npc;
    private final double radius;
    private CancelReason reason;
    private Entity target;

    private TargetNearbyEntityGoal(NPC npc, boolean aggressive, double radius, Function<Entity, Boolean> filter) {
        this.npc = npc;
        this.filter = filter;
        this.aggressive = aggressive;
        this.radius = radius;
    }

    @Override
    public void reset() {
        npc.getNavigator().cancelNavigation();
        target = null;
        finished = false;
        reason = null;
    }

    @Override
    public BehaviorStatus run() {
        if (finished)
            return reason == null ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
        return BehaviorStatus.RUNNING;
    }

    @Override
    public boolean shouldExecute() {
        if (!npc.isSpawned())
            return false;
        List<Entity> nearby = npc.getEntity().getNearbyEntities(radius, radius, radius);
        this.target = null;
        Location npcLoc = npc.getEntity().getLocation();
        Location cache = new Location(null, 0, 0, 0);
        nearby.sort((a, b) -> Double.compare(a.getLocation(cache).distanceSquared(npcLoc),
                b.getLocation(cache).distanceSquared(npcLoc)));
        for (Entity entity : nearby) {
            if (filter.apply(entity)) {
                target = entity;
                break;
            }
        }
        if (target != null) {
            npc.getNavigator().setTarget(target, aggressive);
            npc.getNavigator().getLocalParameters().addSingleUseCallback(cancelReason -> {
                reason = cancelReason;
                finished = true;
            });
            return true;
        }
        return false;
    }

    public static class Builder {
        private boolean aggressive;
        private Function<Entity, Boolean> filter = e -> false;
        private final NPC npc;
        private double radius = 10D;

        public Builder(NPC npc) {
            this.npc = npc;
        }

        public Builder aggressive(boolean aggressive) {
            this.aggressive = aggressive;
            return this;
        }

        public TargetNearbyEntityGoal build() {
            return new TargetNearbyEntityGoal(npc, aggressive, radius, filter);
        }

        public Builder radius(double radius) {
            this.radius = radius;
            return this;
        }

        public Builder targetFilter(Function<Entity, Boolean> filter) {
            this.filter = filter;
            return this;
        }

        public Builder targets(Set<EntityType> targetTypes) {
            this.filter = e -> targetTypes.contains(e.getType());
            return this;
        }
    }

    public static Builder builder(NPC npc) {
        return new Builder(npc);
    }
}
