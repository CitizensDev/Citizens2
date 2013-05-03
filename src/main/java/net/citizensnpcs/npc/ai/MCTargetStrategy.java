package net.citizensnpcs.npc.ai;

import java.lang.reflect.Field;

import net.citizensnpcs.api.ai.AttackStrategy;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerAnimation;
import net.minecraft.server.v1_5_R3.Entity;
import net.minecraft.server.v1_5_R3.EntityLiving;
import net.minecraft.server.v1_5_R3.EntityPlayer;
import net.minecraft.server.v1_5_R3.Navigation;
import net.minecraft.server.v1_5_R3.PathEntity;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class MCTargetStrategy implements PathStrategy, EntityTarget {
    private final boolean aggro;
    private int attackTicks;
    private CancelReason cancelReason;
    private final EntityLiving handle;
    private final Navigation navigation;
    private final NavigatorParameters parameters;
    private final Entity target;
    private final NavigationFieldWrapper wrapper;

    public MCTargetStrategy(NPC handle, org.bukkit.entity.Entity target, boolean aggro, NavigatorParameters params) {
        this.handle = ((CraftLivingEntity) handle.getBukkitEntity()).getHandle();
        this.target = ((CraftEntity) target).getHandle();
        this.navigation = this.handle.getNavigation();
        this.aggro = aggro;
        this.parameters = params;
        this.wrapper = new NavigationFieldWrapper(this.navigation);
    }

    private boolean canAttack() {
        return attackTicks == 0
                && (handle.boundingBox.e > target.boundingBox.b && handle.boundingBox.b < target.boundingBox.e)
                && distanceSquared() <= ATTACK_DISTANCE && hasLineOfSight();
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
        navigation.a(wrapper.findPath(handle, target), parameters.speed());
    }

    @Override
    public void stop() {
        navigation.g();
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
        navigation.a(parameters.avoidWater());
        setPath();
        NMS.look(handle, target);
        if (aggro && canAttack()) {
            AttackStrategy strategy = parameters.attackStrategy();
            if (strategy != null && strategy.handle((LivingEntity) handle.getBukkitEntity(), getTarget())) {
            } else if (handle instanceof EntityPlayer) {
                EntityPlayer humanHandle = (EntityPlayer) handle;
                humanHandle.attack(target);
                PlayerAnimation.ARM_SWING.play(humanHandle.getBukkitEntity());
            } else {
                NMS.attack(handle, target);
            }
            attackTicks = ATTACK_DELAY_TICKS;
        }
        if (attackTicks > 0)
            attackTicks--;

        return false;
    }

    private class NavigationFieldWrapper {
        float e;
        boolean j = true, k, l, m;

        private NavigationFieldWrapper(Navigation navigation) {
            this.k = navigation.c();
            this.l = navigation.a();
            try {
                if (NAV_E != null)
                    e = NAV_E.getFloat(navigation);
                if (NAV_J != null)
                    j = NAV_J.getBoolean(navigation);
                if (NAV_M != null)
                    m = NAV_M.getBoolean(navigation);
            } catch (Exception ex) {
                e = parameters.speed();
            }
        }

        public PathEntity findPath(Entity from, Entity to) {
            return handle.world.findPath(from, to, e, j, k, l, m);
        }
    }

    private static final int ATTACK_DELAY_TICKS = 20;
    private static final double ATTACK_DISTANCE = 1.75 * 1.75;
    private static final Location HANDLE_LOCATION = new Location(null, 0, 0, 0);
    private static Field NAV_E, NAV_J, NAV_M;
    private static final Location TARGET_LOCATION = new Location(null, 0, 0, 0);

    static {
        NAV_E = NMS.getField(Navigation.class, "e");
        NAV_J = NMS.getField(Navigation.class, "j");
        NAV_M = NMS.getField(Navigation.class, "m");
    }
}