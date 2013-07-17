package net.citizensnpcs.npc.ai;

import java.lang.reflect.Field;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.AttackStrategy;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.nms.PlayerNavigation;
import net.minecraft.server.v1_6_R2.AttributeInstance;
import net.minecraft.server.v1_6_R2.Entity;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.EntityPlayer;
import net.minecraft.server.v1_6_R2.Navigation;
import net.minecraft.server.v1_6_R2.PathEntity;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class MCTargetStrategy implements PathStrategy, EntityTarget {
    private final boolean aggro;
    private int attackTicks;
    private CancelReason cancelReason;
    private final EntityLiving handle;
    private final NPC npc;
    private final NavigatorParameters parameters;
    private final Entity target;
    private final TargetNavigator targetNavigator;

    public MCTargetStrategy(NPC npc, org.bukkit.entity.Entity target, boolean aggro, NavigatorParameters params) {
        this.npc = npc;
        this.parameters = params;
        this.handle = ((CraftLivingEntity) npc.getBukkitEntity()).getHandle();
        this.target = ((CraftEntity) target).getHandle();
        Navigation nav = NMS.getNavigation(this.handle);
        this.targetNavigator = nav != null && !params.useNewPathfinder() ? new NavigationFieldWrapper(nav)
                : new AStarTargeter();
        this.aggro = aggro;
    }

    private boolean canAttack() {
        return attackTicks == 0
                && (handle.boundingBox.e > target.boundingBox.b && handle.boundingBox.b < target.boundingBox.e)
                && distanceSquared() <= Setting.NPC_ATTACK_DISTANCE.asDouble() && hasLineOfSight();
    }

    @Override
    public void clearCancelReason() {
        cancelReason = null;
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
        if (cancelReason != null)
            return true;
        setPath();
        NMS.look(handle, target);
        if (aggro && canAttack()) {
            AttackStrategy strategy = parameters.attackStrategy();
            if (strategy != null && strategy.handle((LivingEntity) handle.getBukkitEntity(), getTarget())) {
            }
            attackTicks = ATTACK_DELAY_TICKS;
        }
        if (attackTicks > 0)
            attackTicks--;

        return false;
    }

    private class AStarTargeter implements TargetNavigator {
        private int failureTimes = 0;
        private AStarNavigationStrategy strategy = new AStarNavigationStrategy(npc, target.getBukkitEntity()
                .getLocation(TARGET_LOCATION), parameters);

        @Override
        public void setPath() {
            strategy = new AStarNavigationStrategy(npc, target.getBukkitEntity().getLocation(TARGET_LOCATION),
                    parameters);
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

        @Override
        public void stop() {
            strategy.stop();
        }
    }

    private class NavigationFieldWrapper implements TargetNavigator {
        boolean j = true, k, l, m;
        private final Navigation navigation;
        float range;

        private NavigationFieldWrapper(Navigation navigation) {
            this.navigation = navigation;
            this.k = navigation.c();
            this.l = navigation.a();
            try {
                if (navigation instanceof PlayerNavigation) {
                    if (P_NAV_E != null)
                        range = (float) ((AttributeInstance) P_NAV_E.get(navigation)).getValue();
                    if (P_NAV_J != null)
                        j = P_NAV_J.getBoolean(navigation);
                    if (P_NAV_M != null)
                        m = P_NAV_M.getBoolean(navigation);
                } else {
                    if (E_NAV_E != null)
                        range = (float) ((AttributeInstance) E_NAV_E.get(navigation)).getValue();
                    if (E_NAV_J != null)
                        j = E_NAV_J.getBoolean(navigation);
                    if (E_NAV_M != null)
                        m = E_NAV_M.getBoolean(navigation);
                }
            } catch (Exception ex) {
                range = parameters.range();
            }
        }

        public PathEntity findPath(Entity from, Entity to) {
            return handle.world.findPath(from, to, range, j, k, l, m);
        }

        @Override
        public void setPath() {
            navigation.a(parameters.avoidWater());
            navigation.a(findPath(handle, target), parameters.speed());
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
    private static Field E_NAV_E, E_NAV_J, E_NAV_M;
    private static final Location HANDLE_LOCATION = new Location(null, 0, 0, 0);
    private static Field P_NAV_E, P_NAV_J, P_NAV_M;
    private static final Location TARGET_LOCATION = new Location(null, 0, 0, 0);

    static {
        E_NAV_E = NMS.getField(Navigation.class, "e");
        E_NAV_J = NMS.getField(Navigation.class, "j");
        E_NAV_M = NMS.getField(Navigation.class, "m");
        P_NAV_E = NMS.getField(PlayerNavigation.class, "e");
        P_NAV_J = NMS.getField(PlayerNavigation.class, "j");
        P_NAV_M = NMS.getField(PlayerNavigation.class, "m");
    }
}