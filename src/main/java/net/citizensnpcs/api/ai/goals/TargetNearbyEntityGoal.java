package net.citizensnpcs.api.ai.goals;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

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
    private boolean finished;
    private final NPC npc;
    private final double radius;
    private CancelReason reason;
    private Entity target;
    private final Set<EntityType> targetTypes;

    private TargetNearbyEntityGoal(NPC npc, Set<EntityType> targets, boolean aggressive, double radius) {
        this.npc = npc;
        this.targetTypes = targets;
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
        if (targetTypes.isEmpty() || !npc.isSpawned())
            return false;
        Collection<Entity> nearby = npc.getEntity().getNearbyEntities(radius, radius, radius);
        this.target = null;
        for (Entity entity : nearby) {
            if (targetTypes.contains(entity.getType())) {
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
        private final NPC npc;
        private double radius = 10D;
        private Set<EntityType> targetTypes = EnumSet.noneOf(EntityType.class);

        public Builder(NPC npc) {
            this.npc = npc;
        }

        public Builder aggressive(boolean aggressive) {
            this.aggressive = aggressive;
            return this;
        }

        public TargetNearbyEntityGoal build() {
            return new TargetNearbyEntityGoal(npc, targetTypes, aggressive, radius);
        }

        public Builder radius(double radius) {
            this.radius = radius;
            return this;
        }

        public Builder targets(Set<EntityType> targetTypes) {
            this.targetTypes = targetTypes;
            return this;
        }
    }

    public static Builder builder(NPC npc) {
        return new Builder(npc);
    }
}
