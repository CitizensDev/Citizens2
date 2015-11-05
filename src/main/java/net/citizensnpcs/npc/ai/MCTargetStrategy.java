package net.citizensnpcs.npc.ai;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

import net.citizensnpcs.api.ai.AttackStrategy;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerAnimation;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.NavigationAbstract;

public class MCTargetStrategy implements PathStrategy, EntityTarget {
    private final boolean aggro;
    private int attackTicks;
    private CancelReason cancelReason;
    private final Entity handle;
    private final NPC npc;
    private final NavigatorParameters parameters;
    private final Entity target;
    private final TargetNavigator targetNavigator;
    private int updateCounter;

    public MCTargetStrategy(NPC npc, org.bukkit.entity.Entity target, boolean aggro, NavigatorParameters params) {
        this.npc = npc;
        this.parameters = params;
        this.handle = ((CraftEntity) npc.getEntity()).getHandle();
        this.target = ((CraftEntity) target).getHandle();
        NavigationAbstract nav = NMS.getNavigation(this.handle);
        this.targetNavigator = nav != null && !params.useNewPathfinder() ? new NavigationFieldWrapper(nav)
                : new AStarTargeter();
        this.aggro = aggro;
    }

    private boolean canAttack() {
        return attackTicks == 0
                && (handle.getBoundingBox().e > target.getBoundingBox().b
                        && handle.getBoundingBox().b < target.getBoundingBox().e)
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
        return handle.getBukkitEntity().getLocation(HANDLE_LOCATION)
                .distanceSquared(target.getBukkitEntity().getLocation(TARGET_LOCATION));
    }

    @Override
    public CancelReason getCancelReason() {
        return cancelReason;
    }

    @Override
    public LivingEntity getTarget() {
        return (LivingEntity) target.getBukkitEntity();
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
        return ((LivingEntity) handle.getBukkitEntity()).hasLineOfSight(target.getBukkitEntity());
    }

    @Override
    public boolean isAggressive() {
        return aggro;
    }

    private void setPath() {
        targetNavigator.setPath();
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
        if (target == null || !target.getBukkitEntity().isValid()) {
            cancelReason = CancelReason.TARGET_DIED;
            return true;
        }
        if (target.world != handle.world) {
            cancelReason = CancelReason.TARGET_MOVED_WORLD;
            return true;
        }
        if (cancelReason != null) {
            return true;
        }
        if (!aggro && distanceSquared() < parameters.distanceMargin()) {
            stop();
        } else if (updateCounter++ > 20) {
            setPath();
            updateCounter = 0;
        }

        NMS.look(handle, target);
        if (aggro && canAttack()) {
            AttackStrategy strategy = parameters.attackStrategy();
            if (strategy != null && strategy.handle((LivingEntity) handle.getBukkitEntity(), getTarget())) {
            } else if (strategy != parameters.defaultAttackStrategy()) {
                parameters.defaultAttackStrategy().handle((LivingEntity) handle.getBukkitEntity(), getTarget());
            }
            attackTicks = ATTACK_DELAY_TICKS;
        }
        if (attackTicks > 0) {
            attackTicks--;
        }

        return false;
    }

    private class AStarTargeter implements TargetNavigator {
        private int failureTimes = 0;
        private PathStrategy strategy;

        public AStarTargeter() {
            setStrategy();
        }

        @Override
        public void setPath() {
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
            Location location = target.getBukkitEntity().getLocation(TARGET_LOCATION);
            strategy = npc.isFlyable() ? new FlyingAStarNavigationStrategy(npc, location, parameters)
                    : new AStarNavigationStrategy(npc, location, parameters);
        }

        @Override
        public void stop() {
            strategy.stop();
        }
    }

    private class NavigationFieldWrapper implements TargetNavigator {
        private final NavigationAbstract navigation;

        private NavigationFieldWrapper(NavigationAbstract navigation) {
            this.navigation = navigation;
        }

        @Override
        public void setPath() {
            navigation.a(target, parameters.speed());
        }

        @Override
        public void stop() {
            NMS.stopNavigation(navigation);
        }
    }

    private static interface TargetNavigator {
        void setPath();

        void stop();
    }

    private static final int ATTACK_DELAY_TICKS = 20;
    static final AttackStrategy DEFAULT_ATTACK_STRATEGY = new AttackStrategy() {
        @Override
        public boolean handle(LivingEntity attacker, LivingEntity bukkitTarget) {
            EntityLiving handle = NMS.getHandle(attacker);
            EntityLiving target = NMS.getHandle(bukkitTarget);
            if (handle instanceof EntityPlayer) {
                EntityPlayer humanHandle = (EntityPlayer) handle;
                humanHandle.attack(target);
                PlayerAnimation.ARM_SWING.play(humanHandle.getBukkitEntity());
            } else {
                NMS.attack(handle, target);
            }
            return false;
        }
    };
    private static final Location HANDLE_LOCATION = new Location(null, 0, 0, 0);
    private static final Location TARGET_LOCATION = new Location(null, 0, 0, 0);
}
