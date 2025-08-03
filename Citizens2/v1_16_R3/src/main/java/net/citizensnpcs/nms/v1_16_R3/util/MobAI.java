package net.citizensnpcs.nms.v1_16_R3.util;

import java.util.Map;

import com.google.common.collect.Maps;

import net.citizensnpcs.Settings.Setting;
import net.minecraft.server.v1_16_R3.ControllerJump;
import net.minecraft.server.v1_16_R3.ControllerMove;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityInsentient;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.NavigationAbstract;
import net.minecraft.server.v1_16_R3.PathType;

public interface MobAI {
    org.bukkit.entity.Entity getBukkitEntity();

    ControllerJump getJumpControl();

    Map<PathType, Float> getMalus();

    ControllerMove getMoveControl();

    NavigationAbstract getNavigation();

    default float getPathfindingMalus(PathType var1) {
        Map<PathType, Float> malus = getMalus();
        return malus.containsKey(var1) ? malus.get(var1) : var1.a();
    }

    default void setPathfindingMalus(PathType water, float oldWaterCost) {
        getMalus().put(water, oldWaterCost);
    }

    default void tickAI() {
        getJumpControl().b();
        getMoveControl().a();
        NavigationAbstract navigation = getNavigation();
        if (!NMSImpl.isNavigationFinished(navigation)) {
            NMSImpl.updateNavigation(navigation);
        }
    }

    default void updatePathfindingRange(float range) {
        ((EntityLiving) NMSImpl.getHandle(getBukkitEntity())).getAttributeInstance(GenericAttributes.FOLLOW_RANGE)
                .setValue(range);
    }

    public static class BasicMobAI implements MobAI {
        private final EntityJumpControl controllerJump;
        private final EntityMoveControl controllerMove;
        private final EntityLiving entity;
        private final Map<PathType, Float> malus;
        private final EntityNavigation navigation;

        public BasicMobAI(EntityLiving entity) {
            this.entity = entity;
            NMSImpl.setAttribute(entity, GenericAttributes.FOLLOW_RANGE, Setting.DEFAULT_PATHFINDING_RANGE.asDouble());
            entity.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.3D);
            controllerJump = new EntityJumpControl(entity);
            controllerMove = new EntityMoveControl(entity);
            navigation = new EntityNavigation(entity, entity.world);
            malus = Maps.newEnumMap(PathType.class);
        }

        @Override
        public org.bukkit.entity.Entity getBukkitEntity() {
            return entity.getBukkitEntity();
        }

        @Override
        public ControllerJump getJumpControl() {
            return controllerJump;
        }

        @Override
        public Map<PathType, Float> getMalus() {
            return malus;
        }

        @Override
        public ControllerMove getMoveControl() {
            return controllerMove;
        }

        @Override
        public NavigationAbstract getNavigation() {
            return navigation;
        }

    }

    public static interface ForwardingMobAI extends MobAI {
        MobAI getAI();

        @Override
        default org.bukkit.entity.Entity getBukkitEntity() {
            return getAI().getBukkitEntity();
        }

        @Override
        default ControllerJump getJumpControl() {
            return getAI().getJumpControl();
        }

        @Override
        default Map<PathType, Float> getMalus() {
            return getAI().getMalus();
        }

        @Override
        default ControllerMove getMoveControl() {
            return getAI().getMoveControl();
        }

        @Override
        default NavigationAbstract getNavigation() {
            return getAI().getNavigation();
        }
    }

    public static MobAI from(Entity handle) {
        if (handle instanceof EntityInsentient) {
            EntityInsentient mob = (EntityInsentient) handle;
            return new MobAI() {
                @Override
                public org.bukkit.entity.Entity getBukkitEntity() {
                    return mob.getBukkitEntity();
                }

                @Override
                public ControllerJump getJumpControl() {
                    return mob.getControllerJump();
                }

                @Override
                public Map<PathType, Float> getMalus() {
                    return null;
                }

                @Override
                public ControllerMove getMoveControl() {
                    return mob.getControllerMove();
                }

                @Override
                public NavigationAbstract getNavigation() {
                    return mob.getNavigation();
                }

                @Override
                public float getPathfindingMalus(PathType var1) {
                    return mob.a(var1);
                }

                @Override
                public void setPathfindingMalus(PathType water, float oldWaterCost) {
                    mob.a(water, oldWaterCost);
                }

                @Override
                public void tickAI() {
                    mob.getEntitySenses().a();
                    NMSImpl.updateNavigation(mob.getNavigation());
                    mob.getControllerMove().a();
                    mob.getControllerLook().a();
                    mob.getControllerJump().b();
                }
            };
        } else if (handle instanceof MobAI)
            return (MobAI) handle;
        return null;
    }
}
