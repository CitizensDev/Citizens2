package net.citizensnpcs.nms.v1_19_R3.util;

import java.util.Map;

import com.google.common.collect.Maps;

import net.citizensnpcs.Settings.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public interface MobAI {
    org.bukkit.entity.Entity getBukkitEntity();

    JumpControl getJumpControl();

    Map<BlockPathTypes, Float> getMalus();

    MoveControl getMoveControl();

    PathNavigation getNavigation();

    default float getPathfindingMalus(BlockPathTypes var1) {
        Map<BlockPathTypes, Float> malus = getMalus();
        return malus.containsKey(var1) ? malus.get(var1) : var1.getMalus();
    }

    default void setPathfindingMalus(BlockPathTypes water, float oldWaterCost) {
        getMalus().put(water, oldWaterCost);
    }

    default void tickAI() {
        getJumpControl().tick();
        getMoveControl().tick();
        PathNavigation nav = getNavigation();
        if (!nav.isDone()) {
            nav.tick();
        }
    }

    default void updatePathfindingRange(float range) {
        ((LivingEntity) NMSImpl.getHandle(getBukkitEntity())).getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(range);
    }

    public static class BasicMobAI implements MobAI {
        private final EntityJumpControl controllerJump;
        private final EntityMoveControl controllerMove;
        private final LivingEntity entity;
        private final Map<BlockPathTypes, Float> malus;
        private final EntityNavigation navigation;

        public BasicMobAI(LivingEntity entity) {
            this.entity = entity;
            NMSImpl.setAttribute(entity, Attributes.FOLLOW_RANGE, Setting.DEFAULT_PATHFINDING_RANGE.asDouble());
            entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3D);
            controllerJump = new EntityJumpControl(entity);
            controllerMove = new EntityMoveControl(entity);
            navigation = new EntityNavigation(entity, entity.level);
            malus = Maps.newEnumMap(BlockPathTypes.class);
        }

        @Override
        public org.bukkit.entity.Entity getBukkitEntity() {
            return entity.getBukkitEntity();
        }

        @Override
        public JumpControl getJumpControl() {
            return controllerJump;
        }

        @Override
        public Map<BlockPathTypes, Float> getMalus() {
            return malus;
        }

        @Override
        public MoveControl getMoveControl() {
            return controllerMove;
        }

        @Override
        public PathNavigation getNavigation() {
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
        default JumpControl getJumpControl() {
            return getAI().getJumpControl();
        }

        @Override
        default Map<BlockPathTypes, Float> getMalus() {
            return getAI().getMalus();
        }

        @Override
        default MoveControl getMoveControl() {
            return getAI().getMoveControl();
        }

        @Override
        default PathNavigation getNavigation() {
            return getAI().getNavigation();
        }
    }

    public static MobAI from(Entity handle) {
        if (handle instanceof Mob) {
            Mob mob = (Mob) handle;
            return new MobAI() {
                @Override
                public org.bukkit.entity.Entity getBukkitEntity() {
                    return mob.getBukkitEntity();
                }

                @Override
                public JumpControl getJumpControl() {
                    return mob.getJumpControl();
                }

                @Override
                public Map<BlockPathTypes, Float> getMalus() {
                    return null;
                }

                @Override
                public MoveControl getMoveControl() {
                    return mob.getMoveControl();
                }

                @Override
                public PathNavigation getNavigation() {
                    return mob.getNavigation();
                }

                @Override
                public float getPathfindingMalus(BlockPathTypes var1) {
                    return mob.getPathfindingMalus(var1);
                }

                @Override
                public void setPathfindingMalus(BlockPathTypes water, float oldWaterCost) {
                    mob.setPathfindingMalus(water, oldWaterCost);
                }

                @Override
                public void tickAI() {
                    mob.getSensing().tick();
                    mob.getNavigation().tick();
                    mob.getMoveControl().tick();
                    mob.getLookControl().tick();
                    mob.getJumpControl().tick();
                }
            };
        } else if (handle instanceof MobAI)
            return (MobAI) handle;
        return null;
    }
}
