package net.citizensnpcs.nms.v1_20_R3.util;

import java.util.EnumSet;
import java.util.Iterator;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EntityNodeEvaluator extends EntityNodeEvaluatorBase {
    private final Object2BooleanMap collisionCache = new Object2BooleanOpenHashMap();
    protected float oldWaterCost;
    private final Long2ObjectMap pathTypesByPosCache = new Long2ObjectOpenHashMap();

    private boolean canReachWithoutCollision(Node var0) {
        AABB var1 = this.mob.getBoundingBox();
        Vec3 var2 = new Vec3(var0.x - this.mob.getX() + var1.getXsize() / 2.0,
                var0.y - this.mob.getY() + var1.getYsize() / 2.0, var0.z - this.mob.getZ() + var1.getZsize() / 2.0);
        int var3 = Mth.ceil(var2.length() / var1.getSize());
        var2 = var2.scale(1.0F / var3);

        for (int var4 = 1; var4 <= var3; ++var4) {
            var1 = var1.move(var2);
            if (this.hasCollisions(var1))
                return false;
        }
        return true;
    }

    protected boolean canStartAt(BlockPos var0) {
        BlockPathTypes var1 = this.getBlockPathType(this.mob, var0);
        return var1 != BlockPathTypes.OPEN && this.mvmt.getPathfindingMalus(var1) >= 0.0F;
    }

    @Override
    public void done() {
        this.mvmt.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        this.pathTypesByPosCache.clear();
        this.collisionCache.clear();
        super.done();
    }

    protected BlockPathTypes evaluateBlockPathType(BlockGetter var0, BlockPos var1, BlockPathTypes var2) {
        boolean var3 = this.canPassDoors();
        if (var2 == BlockPathTypes.DOOR_WOOD_CLOSED && this.canOpenDoors() && var3) {
            var2 = BlockPathTypes.WALKABLE_DOOR;
        }
        if (var2 == BlockPathTypes.DOOR_OPEN && !var3) {
            var2 = BlockPathTypes.BLOCKED;
        }
        if (var2 == BlockPathTypes.RAIL && !(var0.getBlockState(var1).getBlock() instanceof BaseRailBlock)
                && !(var0.getBlockState(var1.below()).getBlock() instanceof BaseRailBlock)) {
            var2 = BlockPathTypes.UNPASSABLE_RAIL;
        }
        return var2;
    }

    protected Node findAcceptedNode(int var0, int var1, int var2, int var3, double var4, Direction var6,
            BlockPathTypes var7) {
        Node var8 = null;
        BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos();
        double var10 = this.getFloorLevel(var9.set(var0, var1, var2));
        if (var10 - var4 > this.getMobJumpHeight()) {
            return null;
        } else {
            BlockPathTypes var12 = this.getCachedBlockType(this.mob, var0, var1, var2);
            float var13 = this.mvmt.getPathfindingMalus(var12);
            double var14 = this.mob.getBbWidth() / 2.0;
            if (var13 >= 0.0F) {
                var8 = this.getNodeAndUpdateCostToMax(var0, var1, var2, var12, var13);
            }
            if (doesBlockHavePartialCollision(var7) && var8 != null && var8.costMalus >= 0.0F
                    && !this.canReachWithoutCollision(var8)) {
                var8 = null;
            }
            if (var12 == BlockPathTypes.WALKABLE || this.isAmphibious() && var12 == BlockPathTypes.WATER) {
                return var8;
            } else {
                if ((var8 == null || var8.costMalus < 0.0F) && var3 > 0
                        && (var12 != BlockPathTypes.FENCE || this.canWalkOverFences())
                        && var12 != BlockPathTypes.UNPASSABLE_RAIL && var12 != BlockPathTypes.TRAPDOOR
                        && var12 != BlockPathTypes.POWDER_SNOW) {
                    var8 = this.findAcceptedNode(var0, var1 + 1, var2, var3 - 1, var4, var6, var7);
                    if (var8 != null && (var8.type == BlockPathTypes.OPEN || var8.type == BlockPathTypes.WALKABLE)
                            && this.mob.getBbWidth() < 1.0F) {
                        double var16 = var0 - var6.getStepX() + 0.5;
                        double var18 = var2 - var6.getStepZ() + 0.5;
                        AABB var20 = new AABB(var16 - var14,
                                this.getFloorLevel(var9.set(var16, var1 + 1, var18)) + 0.001, var18 - var14,
                                var16 + var14,
                                this.mob.getBbHeight() + this
                                        .getFloorLevel(var9.set((double) var8.x, (double) var8.y, (double) var8.z))
                                        - 0.002,
                                var18 + var14);
                        if (this.hasCollisions(var20)) {
                            var8 = null;
                        }
                    }
                }
                if (!this.isAmphibious() && var12 == BlockPathTypes.WATER && !this.canFloat()) {
                    if (this.getCachedBlockType(this.mob, var0, var1 - 1, var2) != BlockPathTypes.WATER) {
                        return var8;
                    }
                    while (var1 > this.mob.level().getMinBuildHeight()) {
                        --var1;
                        var12 = this.getCachedBlockType(this.mob, var0, var1, var2);
                        if (var12 != BlockPathTypes.WATER) {
                            return var8;
                        }
                        var8 = this.getNodeAndUpdateCostToMax(var0, var1, var2, var12,
                                this.mvmt.getPathfindingMalus(var12));
                    }
                }
                if (var12 == BlockPathTypes.OPEN) {
                    int var16 = 0;
                    int var17 = var1;

                    while (var12 == BlockPathTypes.OPEN) {
                        --var1;
                        if (var1 < this.mob.level().getMinBuildHeight()) {
                            return this.getBlockedNode(var0, var17, var2);
                        }
                        if (var16++ >= this.mob.getMaxFallDistance()) {
                            return this.getBlockedNode(var0, var1, var2);
                        }
                        var12 = this.getCachedBlockType(this.mob, var0, var1, var2);
                        var13 = this.mvmt.getPathfindingMalus(var12);
                        if (var12 != BlockPathTypes.OPEN && var13 >= 0.0F) {
                            var8 = this.getNodeAndUpdateCostToMax(var0, var1, var2, var12, var13);
                            break;
                        }
                        if (var13 < 0.0F) {
                            return this.getBlockedNode(var0, var1, var2);
                        }
                    }
                }
                if (doesBlockHavePartialCollision(var12) && var8 == null) {
                    var8 = this.getNode(var0, var1, var2);
                    var8.closed = true;
                    var8.type = var12;
                    var8.costMalus = var12.getMalus();
                }
                return var8;
            }
        }
    }

    private Node getBlockedNode(int var0, int var1, int var2) {
        Node var3 = this.getNode(var0, var1, var2);
        var3.type = BlockPathTypes.BLOCKED;
        var3.costMalus = -1.0F;
        return var3;
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter var0, int var1, int var2, int var3) {
        return getBlockPathTypeStatic(var0, new BlockPos.MutableBlockPos(var1, var2, var3));
    }

    public BlockPathTypes getBlockPathType(BlockGetter var0, int var1, int var2, int var3, LivingEntity var4) {
        EnumSet var5 = EnumSet.noneOf(BlockPathTypes.class);
        BlockPathTypes var6 = BlockPathTypes.BLOCKED;
        var6 = this.getBlockPathTypes(var0, var1, var2, var3, var5, var6, var4.blockPosition());
        if (var5.contains(BlockPathTypes.FENCE))
            return BlockPathTypes.FENCE;
        else if (var5.contains(BlockPathTypes.UNPASSABLE_RAIL))
            return BlockPathTypes.UNPASSABLE_RAIL;
        else {
            BlockPathTypes var7 = BlockPathTypes.BLOCKED;
            Iterator var9 = var5.iterator();

            while (var9.hasNext()) {
                BlockPathTypes varr9 = (BlockPathTypes) var9.next();
                if (mvmt.getPathfindingMalus(varr9) < 0.0F)
                    return varr9;

                if (mvmt.getPathfindingMalus(varr9) >= mvmt.getPathfindingMalus(var7)) {
                    var7 = varr9;
                }
            }
            if (var6 == BlockPathTypes.OPEN && mvmt.getPathfindingMalus(var7) == 0.0F && this.entityWidth <= 1)
                return BlockPathTypes.OPEN;
            else
                return var7;
        }
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter var0, int var1, int var2, int var3, Mob var4) {
        EnumSet var5 = EnumSet.noneOf(BlockPathTypes.class);
        BlockPathTypes var6 = BlockPathTypes.BLOCKED;
        var6 = this.getBlockPathTypes(var0, var1, var2, var3, var5, var6, var4.blockPosition());
        if (var5.contains(BlockPathTypes.FENCE))
            return BlockPathTypes.FENCE;
        else if (var5.contains(BlockPathTypes.UNPASSABLE_RAIL))
            return BlockPathTypes.UNPASSABLE_RAIL;
        else {
            BlockPathTypes var7 = BlockPathTypes.BLOCKED;
            Iterator var9 = var5.iterator();

            while (var9.hasNext()) {
                BlockPathTypes varr9 = (BlockPathTypes) var9.next();
                if (mvmt.getPathfindingMalus(varr9) < 0.0F)
                    return varr9;

                if (mvmt.getPathfindingMalus(varr9) >= mvmt.getPathfindingMalus(var7)) {
                    var7 = varr9;
                }
            }
            if (var6 == BlockPathTypes.OPEN && mvmt.getPathfindingMalus(var7) == 0.0F && this.entityWidth <= 1)
                return BlockPathTypes.OPEN;
            else
                return var7;
        }
    }

    protected BlockPathTypes getBlockPathType(LivingEntity var0, BlockPos var1) {
        return this.getCachedBlockType(var0, var1.getX(), var1.getY(), var1.getZ());
    }

    public BlockPathTypes getBlockPathTypes(BlockGetter var0, int var1, int var2, int var3, EnumSet var4,
            BlockPathTypes var5, BlockPos var6) {
        for (int var7 = 0; var7 < this.entityWidth; ++var7) {
            for (int var8 = 0; var8 < this.entityHeight; ++var8) {
                for (int var9 = 0; var9 < this.entityDepth; ++var9) {
                    int var10 = var7 + var1;
                    int var11 = var8 + var2;
                    int var12 = var9 + var3;
                    BlockPathTypes var13 = this.getBlockPathType(var0, var10, var11, var12);
                    var13 = this.evaluateBlockPathType(var0, var6, var13);
                    if (var7 == 0 && var8 == 0 && var9 == 0) {
                        var5 = var13;
                    }
                    var4.add(var13);
                }
            }
        }
        return var5;
    }

    protected BlockPathTypes getCachedBlockType(LivingEntity var0, int var1, int var2, int var3) {
        return (BlockPathTypes) this.pathTypesByPosCache.computeIfAbsent(BlockPos.asLong(var1, var2, var3),
                var4 -> this.getBlockPathType(this.level, var1, var2, var3, var0));
    }

    protected double getFloorLevel(BlockPos var0) {
        return (this.canFloat() || this.isAmphibious()) && this.level.getFluidState(var0).is(FluidTags.WATER)
                ? var0.getY() + 0.5
                : getFloorLevel(this.level, var0);
    }

    @Override
    public Target getGoal(double var0, double var2, double var4) {
        return this.getTargetFromNode(this.getNode(Mth.floor(var0), Mth.floor(var2), Mth.floor(var4)));
    }

    private double getMobJumpHeight() {
        return Math.max(1.125, this.mob.maxUpStep());
    }

    @Override
    public int getNeighbors(Node[] var0, Node var1) {
        int var2 = 0;
        int var3 = 0;
        BlockPathTypes var4 = this.getCachedBlockType(this.mob, var1.x, var1.y + 1, var1.z);
        BlockPathTypes var5 = this.getCachedBlockType(this.mob, var1.x, var1.y, var1.z);
        if (this.mvmt.getPathfindingMalus(var4) >= 0.0F && var5 != BlockPathTypes.STICKY_HONEY) {
            var3 = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
        }
        double var6 = this.getFloorLevel(new BlockPos(var1.x, var1.y, var1.z));
        Node var8 = this.findAcceptedNode(var1.x, var1.y, var1.z + 1, var3, var6, Direction.SOUTH, var5);
        if (this.isNeighborValid(var8, var1)) {
            var0[var2++] = var8;
        }
        Node var9 = this.findAcceptedNode(var1.x - 1, var1.y, var1.z, var3, var6, Direction.WEST, var5);
        if (this.isNeighborValid(var9, var1)) {
            var0[var2++] = var9;
        }
        Node var10 = this.findAcceptedNode(var1.x + 1, var1.y, var1.z, var3, var6, Direction.EAST, var5);
        if (this.isNeighborValid(var10, var1)) {
            var0[var2++] = var10;
        }
        Node var11 = this.findAcceptedNode(var1.x, var1.y, var1.z - 1, var3, var6, Direction.NORTH, var5);
        if (this.isNeighborValid(var11, var1)) {
            var0[var2++] = var11;
        }
        Node var12 = this.findAcceptedNode(var1.x - 1, var1.y, var1.z - 1, var3, var6, Direction.NORTH, var5);
        if (this.isDiagonalValid(var1, var9, var11, var12)) {
            var0[var2++] = var12;
        }
        Node var13 = this.findAcceptedNode(var1.x + 1, var1.y, var1.z - 1, var3, var6, Direction.NORTH, var5);
        if (this.isDiagonalValid(var1, var10, var11, var13)) {
            var0[var2++] = var13;
        }
        Node var14 = this.findAcceptedNode(var1.x - 1, var1.y, var1.z + 1, var3, var6, Direction.SOUTH, var5);
        if (this.isDiagonalValid(var1, var9, var8, var14)) {
            var0[var2++] = var14;
        }
        Node var15 = this.findAcceptedNode(var1.x + 1, var1.y, var1.z + 1, var3, var6, Direction.SOUTH, var5);
        if (this.isDiagonalValid(var1, var10, var8, var15)) {
            var0[var2++] = var15;
        }
        return var2;
    }

    private Node getNodeAndUpdateCostToMax(int var0, int var1, int var2, BlockPathTypes var3, float var4) {
        Node var5 = this.getNode(var0, var1, var2);
        var5.type = var3;
        var5.costMalus = Math.max(var5.costMalus, var4);
        return var5;
    }

    @Override
    public Node getStart() {
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
        int var0 = this.mob.getBlockY();
        BlockState var2 = this.level.getBlockState(var1.set(this.mob.getX(), var0, this.mob.getZ()));
        BlockPos var3;
        if (!this.mob.canStandOnFluid(var2.getFluidState())) {
            if (this.canFloat() && this.mob.isInWater()) {
                while (true) {
                    if (!var2.is(Blocks.WATER) && var2.getFluidState() != Fluids.WATER.getSource(false)) {
                        --var0;
                        break;
                    }
                    ++var0;
                    var2 = this.level.getBlockState(var1.set(this.mob.getX(), var0, this.mob.getZ()));
                }
            } else if (this.mob.onGround()) {
                var0 = Mth.floor(this.mob.getY() + 0.5);
            } else {
                for (var3 = this.mob.blockPosition(); (this.level.getBlockState(var3).isAir()
                        || this.level.getBlockState(var3).isPathfindable(this.level, var3, PathComputationType.LAND))
                        && var3.getY() > this.mob.level().getMinBuildHeight(); var3 = var3.below()) {
                }
                var0 = var3.above().getY();
            }
        } else {
            while (true) {
                if (!this.mob.canStandOnFluid(var2.getFluidState())) {
                    --var0;
                    break;
                }
                ++var0;
                var2 = this.level.getBlockState(var1.set(this.mob.getX(), var0, this.mob.getZ()));
            }
        }
        var3 = this.mob.blockPosition();
        if (!this.canStartAt(var1.set(var3.getX(), var0, var3.getZ()))) {
            AABB var4 = this.mob.getBoundingBox();
            if (this.canStartAt(var1.set(var4.minX, var0, var4.minZ))
                    || this.canStartAt(var1.set(var4.minX, var0, var4.maxZ))
                    || this.canStartAt(var1.set(var4.maxX, var0, var4.minZ))
                    || this.canStartAt(var1.set(var4.maxX, var0, var4.maxZ))) {
                return this.getStartNode(var1);
            }
        }
        return this.getStartNode(new BlockPos(var3.getX(), var0, var3.getZ()));
    }

    protected Node getStartNode(BlockPos var0) {
        Node var1 = this.getNode(var0);
        var1.type = this.getBlockPathType(this.mob, var1.asBlockPos());
        var1.costMalus = this.mvmt.getPathfindingMalus(var1.type);
        return var1;
    }

    private boolean hasCollisions(AABB var0) {
        return this.collisionCache.computeIfAbsent(var0, var1 -> !this.level.noCollision(this.mob, var0));
    }

    protected boolean isAmphibious() {
        return false;
    }

    protected boolean isDiagonalValid(Node var0, Node var1, Node var2, Node var3) {
        if (var3 != null && var2 != null && var1 != null) {
            if (var3.closed) {
                return false;
            } else if (var2.y <= var0.y && var1.y <= var0.y) {
                if (var1.type != BlockPathTypes.WALKABLE_DOOR && var2.type != BlockPathTypes.WALKABLE_DOOR
                        && var3.type != BlockPathTypes.WALKABLE_DOOR) {
                    boolean var4 = var2.type == BlockPathTypes.FENCE && var1.type == BlockPathTypes.FENCE
                            && this.mob.getBbWidth() < 0.5;
                    return var3.costMalus >= 0.0F && (var2.y < var0.y || var2.costMalus >= 0.0F || var4)
                            && (var1.y < var0.y || var1.costMalus >= 0.0F || var4);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    protected boolean isNeighborValid(Node var0, Node var1) {
        return var0 != null && !var0.closed && (var0.costMalus >= 0.0F || var1.costMalus < 0.0F);
    }

    @Override
    public void prepare(PathNavigationRegion var0, LivingEntity var1) {
        super.prepare(var0, var1);
        this.oldWaterCost = mvmt.getPathfindingMalus(BlockPathTypes.WATER);
    }

    @Override
    public void prepare(PathNavigationRegion var0, Mob var1) {
        super.prepare(var0, var1);
        this.oldWaterCost = mvmt.getPathfindingMalus(BlockPathTypes.WATER);
    }

    public static BlockPathTypes checkNeighbourBlocks(BlockGetter var0, BlockPos.MutableBlockPos var1,
            BlockPathTypes var2) {
        int var3 = var1.getX();
        int var4 = var1.getY();
        int var5 = var1.getZ();

        for (int var6 = -1; var6 <= 1; ++var6) {
            for (int var7 = -1; var7 <= 1; ++var7) {
                for (int var8 = -1; var8 <= 1; ++var8) {
                    if (var6 != 0 || var8 != 0) {
                        var1.set(var3 + var6, var4 + var7, var5 + var8);
                        BlockState var9 = var0.getBlockState(var1);
                        if (var9.is(Blocks.CACTUS) || var9.is(Blocks.SWEET_BERRY_BUSH))
                            return BlockPathTypes.DANGER_OTHER;

                        if (isBurningBlock(var9))
                            return BlockPathTypes.DANGER_FIRE;

                        if (var0.getFluidState(var1).is(FluidTags.WATER))
                            return BlockPathTypes.WATER_BORDER;

                        if (var9.is(Blocks.WITHER_ROSE) || var9.is(Blocks.POINTED_DRIPSTONE))
                            return BlockPathTypes.DAMAGE_CAUTIOUS;
                    }
                }
            }
        }
        return var2;
    }

    private static boolean doesBlockHavePartialCollision(BlockPathTypes var0) {
        return var0 == BlockPathTypes.FENCE || var0 == BlockPathTypes.DOOR_WOOD_CLOSED
                || var0 == BlockPathTypes.DOOR_IRON_CLOSED;
    }

    protected static BlockPathTypes getBlockPathTypeRaw(BlockGetter var0, BlockPos var1) {
        BlockState var2 = var0.getBlockState(var1);
        Block var3 = var2.getBlock();
        if (var2.isAir())
            return BlockPathTypes.OPEN;
        else if (!var2.is(BlockTags.TRAPDOORS) && !var2.is(Blocks.LILY_PAD) && !var2.is(Blocks.BIG_DRIPLEAF)) {
            if (var2.is(Blocks.POWDER_SNOW))
                return BlockPathTypes.POWDER_SNOW;
            else if (!var2.is(Blocks.CACTUS) && !var2.is(Blocks.SWEET_BERRY_BUSH)) {
                if (var2.is(Blocks.HONEY_BLOCK))
                    return BlockPathTypes.STICKY_HONEY;
                else if (var2.is(Blocks.COCOA))
                    return BlockPathTypes.COCOA;
                else if (!var2.is(Blocks.WITHER_ROSE) && !var2.is(Blocks.POINTED_DRIPSTONE)) {
                    FluidState var4 = var0.getFluidState(var1);
                    if (var4.is(FluidTags.LAVA))
                        return BlockPathTypes.LAVA;
                    else if (isBurningBlock(var2))
                        return BlockPathTypes.DAMAGE_FIRE;
                    else if (var3 instanceof DoorBlock) {
                        DoorBlock var5 = (DoorBlock) var3;
                        if (var2.getValue(DoorBlock.OPEN))
                            return BlockPathTypes.DOOR_OPEN;
                        else
                            return var5.type().canOpenByHand() ? BlockPathTypes.DOOR_WOOD_CLOSED
                                    : BlockPathTypes.DOOR_IRON_CLOSED;
                    } else if (var3 instanceof BaseRailBlock)
                        return BlockPathTypes.RAIL;
                    else if (var3 instanceof LeavesBlock)
                        return BlockPathTypes.LEAVES;
                    else if (var2.is(BlockTags.FENCES) || var2.is(BlockTags.WALLS)
                            || var3 instanceof FenceGateBlock && !(Boolean) var2.getValue(FenceGateBlock.OPEN))
                        return BlockPathTypes.FENCE;
                    else if (!var2.isPathfindable(var0, var1, PathComputationType.LAND))
                        return BlockPathTypes.BLOCKED;
                    else
                        return var4.is(FluidTags.WATER) ? BlockPathTypes.WATER : BlockPathTypes.OPEN;
                } else
                    return BlockPathTypes.DAMAGE_CAUTIOUS;
            } else
                return BlockPathTypes.DAMAGE_OTHER;
        } else
            return BlockPathTypes.TRAPDOOR;
    }

    public static BlockPathTypes getBlockPathTypeStatic(BlockGetter var0, BlockPos.MutableBlockPos var1) {
        int var2 = var1.getX();
        int var3 = var1.getY();
        int var4 = var1.getZ();
        BlockPathTypes var5 = getBlockPathTypeRaw(var0, var1);
        if (var5 == BlockPathTypes.OPEN && var3 >= var0.getMinBuildHeight() + 1) {
            BlockPathTypes var6 = getBlockPathTypeRaw(var0, var1.set(var2, var3 - 1, var4));
            var5 = var6 != BlockPathTypes.WALKABLE && var6 != BlockPathTypes.OPEN && var6 != BlockPathTypes.WATER
                    && var6 != BlockPathTypes.LAVA ? BlockPathTypes.WALKABLE : BlockPathTypes.OPEN;
            if (var6 == BlockPathTypes.DAMAGE_FIRE) {
                var5 = BlockPathTypes.DAMAGE_FIRE;
            }
            if (var6 == BlockPathTypes.DAMAGE_OTHER) {
                var5 = BlockPathTypes.DAMAGE_OTHER;
            }
            if (var6 == BlockPathTypes.STICKY_HONEY) {
                var5 = BlockPathTypes.STICKY_HONEY;
            }
            if (var6 == BlockPathTypes.POWDER_SNOW) {
                var5 = BlockPathTypes.DANGER_POWDER_SNOW;
            }
            if (var6 == BlockPathTypes.DAMAGE_CAUTIOUS) {
                var5 = BlockPathTypes.DAMAGE_CAUTIOUS;
            }
        }
        if (var5 == BlockPathTypes.WALKABLE) {
            var5 = checkNeighbourBlocks(var0, var1.set(var2, var3, var4), var5);
        }
        return var5;
    }

    public static double getFloorLevel(BlockGetter var0, BlockPos var1) {
        BlockPos var2 = var1.below();
        VoxelShape var3 = var0.getBlockState(var2).getCollisionShape(var0, var2);
        return var2.getY() + (var3.isEmpty() ? 0.0 : var3.max(Axis.Y));
    }

    public static boolean isBurningBlock(BlockState var0) {
        return var0.is(BlockTags.FIRE) || var0.is(Blocks.LAVA) || var0.is(Blocks.MAGMA_BLOCK)
                || CampfireBlock.isLitCampfire(var0) || var0.is(Blocks.LAVA_CAULDRON);
    }

    public static final double SPACE_BETWEEN_WALL_POSTS = 0.5;
}
