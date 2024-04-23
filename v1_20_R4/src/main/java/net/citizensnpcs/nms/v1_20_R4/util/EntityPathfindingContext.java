package net.citizensnpcs.nms.v1_20_R4.util;

import net.citizensnpcs.nms.v1_20_R4.entity.SlimeController.EntitySlimeNPC;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathTypeCache;
import net.minecraft.world.level.pathfinder.PathfindingContext;

public class EntityPathfindingContext extends PathfindingContext {
    private final PathTypeCache cache;
    private final CollisionGetter level;
    private final BlockPos mobPosition;
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    public EntityPathfindingContext(CollisionGetter var0, LivingEntity var1) {
        super(var0, new EntitySlimeNPC(EntityType.SLIME, var1.level()));
        this.level = var0;
        Level var4 = var1.level();
        if (var4 instanceof ServerLevel) {
            this.cache = ((ServerLevel) var4).getPathTypeCache();
        } else {
            this.cache = null;
        }
        this.mobPosition = var1.blockPosition();
    }

    @Override
    public BlockState getBlockState(BlockPos var0) {
        return this.level.getBlockState(var0);
    }

    @Override
    public PathType getPathTypeFromState(int var0, int var1, int var2) {
        BlockPos var3 = this.mutablePos.set(var0, var1, var2);
        return this.cache == null ? getPathTypeFromState(this.level, var3) : this.cache.getOrCompute(this.level, var3);
    }

    @Override
    public CollisionGetter level() {
        return this.level;
    }

    @Override
    public BlockPos mobPosition() {
        return this.mobPosition;
    }

    static PathType getPathTypeFromState(BlockGetter var0, BlockPos var1) {
        BlockState var2 = var0.getBlockState(var1);
        Block var3 = var2.getBlock();
        if (var2.isAir()) {
            return PathType.OPEN;
        } else if (!var2.is(BlockTags.TRAPDOORS) && !var2.is(Blocks.LILY_PAD) && !var2.is(Blocks.BIG_DRIPLEAF)) {
            if (var2.is(Blocks.POWDER_SNOW)) {
                return PathType.POWDER_SNOW;
            } else if (!var2.is(Blocks.CACTUS) && !var2.is(Blocks.SWEET_BERRY_BUSH)) {
                if (var2.is(Blocks.HONEY_BLOCK)) {
                    return PathType.STICKY_HONEY;
                } else if (var2.is(Blocks.COCOA)) {
                    return PathType.COCOA;
                } else if (!var2.is(Blocks.WITHER_ROSE) && !var2.is(Blocks.POINTED_DRIPSTONE)) {
                    FluidState var4 = var2.getFluidState();
                    if (var4.is(FluidTags.LAVA)) {
                        return PathType.LAVA;
                    } else if (isBurningBlock(var2)) {
                        return PathType.DAMAGE_FIRE;
                    } else if (var3 instanceof DoorBlock) {
                        DoorBlock var5 = (DoorBlock) var3;
                        if (var2.getValue(DoorBlock.OPEN)) {
                            return PathType.DOOR_OPEN;
                        } else {
                            return var5.type().canOpenByHand() ? PathType.DOOR_WOOD_CLOSED : PathType.DOOR_IRON_CLOSED;
                        }
                    } else if (var3 instanceof BaseRailBlock) {
                        return PathType.RAIL;
                    } else if (var3 instanceof LeavesBlock) {
                        return PathType.LEAVES;
                    } else if (var2.is(BlockTags.FENCES) || var2.is(BlockTags.WALLS)
                            || var3 instanceof FenceGateBlock && !(Boolean) var2.getValue(FenceGateBlock.OPEN)) {
                        return PathType.FENCE;
                    } else if (!var2.isPathfindable(PathComputationType.LAND)) {
                        return PathType.BLOCKED;
                    } else {
                        return var4.is(FluidTags.WATER) ? PathType.WATER : PathType.OPEN;
                    }
                } else {
                    return PathType.DAMAGE_CAUTIOUS;
                }
            } else {
                return PathType.DAMAGE_OTHER;
            }
        } else {
            return PathType.TRAPDOOR;
        }
    }

    static boolean isBurningBlock(BlockState var0) {
        return var0.is(BlockTags.FIRE) || var0.is(Blocks.LAVA) || var0.is(Blocks.MAGMA_BLOCK)
                || CampfireBlock.isLitCampfire(var0) || var0.is(Blocks.LAVA_CAULDRON);
    }
}
