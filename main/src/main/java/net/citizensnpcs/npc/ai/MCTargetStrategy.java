package net.citizensnpcs.npc.ai;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.ai.AttackStrategy;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.PathStrategy;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.util.NMS;

public class MCTargetStrategy implements PathStrategy, EntityTarget {
    private final boolean aggro;
    private int attackTicks;
    private CancelReason cancelReason;
    private final Entity handle;
    private final NPC npc;
    private final NavigatorParameters parameters;
    private final Entity target;
    private TargetNavigator targetNavigator;
    private int updateCounter = -1;

    public MCTargetStrategy(NPC npc, org.bukkit.entity.Entity target, boolean aggro, NavigatorParameters params) {
        this.npc = npc;
        this.parameters = params;
        this.handle = npc.getEntity();
        this.target = target;
        TargetNavigator nms = NMS.getTargetNavigator(npc.getEntity(), target, params);
        this.targetNavigator = nms != null && !params.useNewPathfinder() ? nms : new AStarTargeter();
        this.aggro = aggro;
    }

    private boolean canAttack() {
        BoundingBox handleBB = NMS.getBoundingBox(handle), targetBB = NMS.getBoundingBox(target);
        return attackTicks == 0 && (handleBB.maxY > targetBB.minY && handleBB.minY < targetBB.maxY)
                && closeEnough(distanceSquared()) && hasLineOfSight();
    }

    @Override
    public void clearCancelReason() {
        cancelReason = null;
    }

    private boolean closeEnough(double distance) {
        return distance <= parameters.attackRange();
    }

    private double distanceSquared() {
        return handle.getLocation(HANDLE_LOCATION).distanceSquared(target.getLocation(TARGET_LOCATION));
    }

    @Override
    public CancelReason getCancelReason() {
        return cancelReason;
    }

    @Override
    public Iterable<Vector> getPath() {
        return targetNavigator.getPath();
    }

    @Override
    public org.bukkit.entity.Entity getTarget() {
        return target;
    }

    @Override
    public Location getTargetAsLocation() {
        return getTarget().getLocation();
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.ENTITY;
    }

    private boolean hasLineOfSight() {
        return ((LivingEntity) handle).hasLineOfSight(target);
    }

    @Override
    public boolean isAggressive() {
        return aggro;
    }

    @Override
    public void stop() {
        targetNavigator.stop();
    }

    @Override
    public String toString() {
        return "MCTargetStrategy [target=" + target + "]";
    }

    @Override
    public boolean update() {
        if (target == null || !target.isValid()) {
            cancelReason = CancelReason.TARGET_DIED;
            return true;
        }
        if (target.getWorld() != handle.getWorld()) {
            cancelReason = CancelReason.TARGET_MOVED_WORLD;
            return true;
        }
        if (cancelReason != null) {
            return true;
        }
        if (parameters.straightLineTargetingDistance() > 0 && !(targetNavigator instanceof StraightLineTargeter)) {
            targetNavigator = new StraightLineTargeter(targetNavigator);
        }
        if (!aggro && distanceSquared() <= parameters.distanceMargin()) {
            stop();
            return false;
        } else if (updateCounter == -1 || updateCounter++ > parameters.updatePathRate()) {
            targetNavigator.setPath();
            updateCounter = 0;
        }
        targetNavigator.update();

        NMS.look(handle, target);
        if (aggro && canAttack()) {
            AttackStrategy strategy = parameters.attackStrategy();
            if (strategy != null && strategy.handle((LivingEntity) handle, (LivingEntity) getTarget())) {
            } else if (strategy != parameters.defaultAttackStrategy()) {
                parameters.defaultAttackStrategy().handle((LivingEntity) handle, (LivingEntity) getTarget());
            }
            attackTicks = parameters.attackDelayTicks();
        }
        if (attackTicks > 0) {
            attackTicks--;
        }

        return false;
    }

    private class AStarTargeter implements TargetNavigator {
        private int failureTimes = 0;
        private PathStrategy strategy;

        @Override
        public Iterable<Vector> getPath() {
            return strategy.getPath();
        }

        @Override
        public void setPath() {
            // TODO: should use fallback-style pathfinding
            setStrategy();
            strategy.update();
            CancelReason subReason = strategy.getCancelReason();
            if (subReason == CancelReason.STUCK) {
                if (failureTimes++ > 10) {
                    cancelReason = strategy.getCancelReason();
                }
            } else {
                failureTimes = 0;
                cancelReason = strategy.getCancelReason();
            }
        }

        private void setStrategy() {
            Location location = parameters.entityTargetLocationMapper().apply(target);
            if (location == null) {
                throw new IllegalStateException("mapper should not return null");
            }
            if (!npc.isFlyable()) {
                Block block = location.getBlock();
                while (!MinecraftBlockExaminer.canStandOn(block.getRelative(BlockFace.DOWN))) {
                    block = block.getRelative(BlockFace.DOWN);
                    if (block.getY() <= 0) {
                        block = location.getBlock();
                        break;
                    }
                }
                location = block.getLocation();
            }
            strategy = npc.isFlyable() ? new FlyingAStarNavigationStrategy(npc, location, parameters)
                    : new AStarNavigationStrategy(npc, location, parameters);
        }

        @Override
        public void stop() {
            if (strategy != null) {
                strategy.stop();
            }
        }

        @Override
        public void update() {
            strategy.update();
        }
    }

    private class StraightLineTargeter implements TargetNavigator {
        private PathStrategy active;
        private final TargetNavigator fallback;

        public StraightLineTargeter(TargetNavigator navigator) {
            fallback = navigator;
        }

        @Override
        public Iterable<Vector> getPath() {
            if (active != null) {
                return active.getPath();
            }
            return fallback.getPath();
        }

        @Override
        public void setPath() {
            Location location = parameters.entityTargetLocationMapper().apply(target);
            if (location == null) {
                throw new IllegalStateException("mapper should not return null");
            }
            if (parameters.straightLineTargetingDistance() > 0) {
                double distance = npc.getStoredLocation().distance(location);
                if (distance < parameters.straightLineTargetingDistance()) {
                    active = new StraightLineNavigationStrategy(npc, location, parameters);
                    return;
                }
            }
            active = null;
        }

        @Override
        public void stop() {
            if (active != null) {
                active.stop();
            }
            fallback.stop();
        }

        @Override
        public void update() {
            if (active != null) {
                active.update();
            }
            fallback.update();
        }
    }

    public static interface TargetNavigator {
        Iterable<Vector> getPath();

        void setPath();

        void stop();

        void update();
    }

    static final AttackStrategy DEFAULT_ATTACK_STRATEGY = new AttackStrategy() {
        @Override
        public boolean handle(LivingEntity attacker, LivingEntity bukkitTarget) {
            NMS.attack(attacker, bukkitTarget);
            return false;
        }
    };
    private static final Location HANDLE_LOCATION = new Location(null, 0, 0, 0);
    private static final Location TARGET_LOCATION = new Location(null, 0, 0, 0);
}
