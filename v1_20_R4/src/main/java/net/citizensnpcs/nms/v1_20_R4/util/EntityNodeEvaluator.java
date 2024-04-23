package net.citizensnpcs.nms.v1_20_R4.util;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EntityNodeEvaluator extends EntityNodeEvaluatorBase {
    private final Object2BooleanMap collisionCache = new Object2BooleanOpenHashMap();
    protected float oldWaterCost;
    private final Long2ObjectMap pathTypesByPosCacheByMob = new Long2ObjectOpenHashMap();
    private final Node[] reusableNeighbors = new Node[Plane.HORIZONTAL.length()];

    private boolean canReachWithoutCollision(Node var0) {
        AABB var1 = this.mob.getBoundingBox();
        Vec3 var2 = new Vec3(var0.x - this.mob.getX() + var1.getXsize() / 2.0,
                var0.y - this.mob.getY() + var1.getYsize() / 2.0, var0.z - this.mob.getZ() + var1.getZsize() / 2.0);
        int var3 = Mth.ceil(var2.length() / var1.getSize());
        var2 = var2.scale(1.0F / var3);

        for (int var4 = 1; var4 <= var3; ++var4) {
            var1 = var1.move(var2);
            if (this.hasCollisions(var1)) {
                return false;
            }
        }
        return true;
    }

    protected boolean canStartAt(BlockPos var0) {
        PathType var1 = this.getCachedPathType(var0.getX(), var0.getY(), var0.getZ());
        return var1 != PathType.OPEN && this.mvmt.getPathfindingMalus(var1) >= 0.0F;
    }

    @Override
    public void done() {
        this.mvmt.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
        this.pathTypesByPosCacheByMob.clear();
        this.collisionCache.clear();
        super.done();
    }

    protected Node findAcceptedNode(int var0, int var1, int var2, int var3, double var4, Direction var6,
            PathType var7) {
        Node var8 = null;
        BlockPos.MutableBlockPos var9 = new BlockPos.MutableBlockPos();
        double var10 = this.getFloorLevel(var9.set(var0, var1, var2));
        if (var10 - var4 > this.getMobJumpHeight()) {
            return null;
        } else {
            PathType var12 = this.getCachedPathType(var0, var1, var2);
            float var13 = this.mvmt.getPathfindingMalus(var12);
            if (var13 >= 0.0F) {
                var8 = this.getNodeAndUpdateCostToMax(var0, var1, var2, var12, var13);
            }
            if (doesBlockHavePartialCollision(var7) && var8 != null && var8.costMalus >= 0.0F
                    && !this.canReachWithoutCollision(var8)) {
                var8 = null;
            }
            if (var12 != PathType.WALKABLE && (!this.isAmphibious() || var12 != PathType.WATER)) {
                if ((var8 == null || var8.costMalus < 0.0F) && var3 > 0
                        && (var12 != PathType.FENCE || this.canWalkOverFences()) && var12 != PathType.UNPASSABLE_RAIL
                        && var12 != PathType.TRAPDOOR && var12 != PathType.POWDER_SNOW) {
                    var8 = this.tryJumpOn(var0, var1, var2, var3, var4, var6, var7, var9);
                } else if (!this.isAmphibious() && var12 == PathType.WATER && !this.canFloat()) {
                    var8 = this.tryFindFirstNonWaterBelow(var0, var1, var2, var8);
                } else if (var12 == PathType.OPEN) {
                    var8 = this.tryFindFirstGroundNodeBelow(var0, var1, var2);
                } else if (doesBlockHavePartialCollision(var12) && var8 == null) {
                    var8 = this.getClosedNode(var0, var1, var2, var12);
                }
                return var8;
            } else {
                return var8;
            }
        }
    }

    private Node getBlockedNode(int var0, int var1, int var2) {
        Node var3 = this.getNode(var0, var1, var2);
        var3.type = PathType.BLOCKED;
        var3.costMalus = -1.0F;
        return var3;
    }

    protected PathType getCachedPathType(int var0, int var1, int var2) {
        return (PathType) this.pathTypesByPosCacheByMob.computeIfAbsent(BlockPos.asLong(var0, var1, var2), (var3) -> {
            return this.getPathTypeOfMob(this.currentContext, var0, var1, var2, this.mob);
        });
    }

    private Node getClosedNode(int var0, int var1, int var2, PathType var3) {
        Node var4 = this.getNode(var0, var1, var2);
        var4.closed = true;
        var4.type = var3;
        var4.costMalus = var3.getMalus();
        return var4;
    }

    protected double getFloorLevel(BlockPos var0) {
        BlockGetter var1 = this.currentContext.level();
        return (this.canFloat() || this.isAmphibious()) && var1.getFluidState(var0).is(FluidTags.WATER)
                ? var0.getY() + 0.5
                : getFloorLevel(var1, var0);
    }

    private double getMobJumpHeight() {
        return Math.max(1.125, this.mob.maxUpStep());
    }

    @Override
    public int getNeighbors(Node[] var0, Node var1) {
        int var2 = 0;
        int var3 = 0;
        PathType var4 = this.getCachedPathType(var1.x, var1.y + 1, var1.z);
        PathType var5 = this.getCachedPathType(var1.x, var1.y, var1.z);
        if (this.mvmt.getPathfindingMalus(var4) >= 0.0F && var5 != PathType.STICKY_HONEY) {
            var3 = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
        }
        double var6 = this.getFloorLevel(new BlockPos(var1.x, var1.y, var1.z));
        Iterator<Direction> var99 = Plane.HORIZONTAL.iterator();

        while (var99.hasNext()) {
            Direction var9 = var99.next();
            Node var10 = this.findAcceptedNode(var1.x + var9.getStepX(), var1.y, var1.z + var9.getStepZ(), var3, var6,
                    var9, var5);
            this.reusableNeighbors[var9.get2DDataValue()] = var10;
            if (this.isNeighborValid(var10, var1)) {
                var0[var2++] = var10;
            }
        }
        var99 = Plane.HORIZONTAL.iterator();

        while (var99.hasNext()) {
            Direction var9 = var99.next();
            Direction var10 = var9.getClockWise();
            if (this.isDiagonalValid(var1, this.reusableNeighbors[var9.get2DDataValue()],
                    this.reusableNeighbors[var10.get2DDataValue()])) {
                Node var11 = this.findAcceptedNode(var1.x + var9.getStepX() + var10.getStepX(), var1.y,
                        var1.z + var9.getStepZ() + var10.getStepZ(), var3, var6, var9, var5);
                if (this.isDiagonalValid(var11)) {
                    var0[var2++] = var11;
                }
            }
        }
        return var2;
    }

    private Node getNodeAndUpdateCostToMax(int var0, int var1, int var2, PathType var3, float var4) {
        Node var5 = this.getNode(var0, var1, var2);
        var5.type = var3;
        var5.costMalus = Math.max(var5.costMalus, var4);
        return var5;
    }

    @Override
    public PathType getPathType(PathfindingContext var0, int var1, int var2, int var3) {
        return getPathTypeStatic(var0, new BlockPos.MutableBlockPos(var1, var2, var3));
    }

    public PathType getPathTypeOfMob(PathfindingContext var0, int var1, int var2, int var3, LivingEntity var4) {
        Set var5 = this.getPathTypeWithinMobBB(var0, var1, var2, var3);
        if (var5.contains(PathType.FENCE)) {
            return PathType.FENCE;
        } else if (var5.contains(PathType.UNPASSABLE_RAIL)) {
            return PathType.UNPASSABLE_RAIL;
        } else {
            PathType var6 = PathType.BLOCKED;
            Iterator<PathType> var88 = var5.iterator();

            while (var88.hasNext()) {
                PathType var8 = var88.next();
                if (mvmt.getPathfindingMalus(var8) < 0.0F) {
                    return var8;
                }
                if (mvmt.getPathfindingMalus(var8) >= mvmt.getPathfindingMalus(var6)) {
                    var6 = var8;
                }
            }
            if (this.entityWidth <= 1 && var6 != PathType.OPEN && mvmt.getPathfindingMalus(var6) == 0.0F
                    && this.getPathType(var0, var1, var2, var3) == PathType.OPEN) {
                return PathType.OPEN;
            } else {
                return var6;
            }
        }
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext var0, int var1, int var2, int var3, Mob var4) {
        Set var5 = this.getPathTypeWithinMobBB(var0, var1, var2, var3);
        if (var5.contains(PathType.FENCE)) {
            return PathType.FENCE;
        } else if (var5.contains(PathType.UNPASSABLE_RAIL)) {
            return PathType.UNPASSABLE_RAIL;
        } else {
            PathType var6 = PathType.BLOCKED;
            Iterator<PathType> var88 = var5.iterator();

            while (var88.hasNext()) {
                PathType var8 = var88.next();
                if (var4.getPathfindingMalus(var8) < 0.0F) {
                    return var8;
                }
                if (var4.getPathfindingMalus(var8) >= var4.getPathfindingMalus(var6)) {
                    var6 = var8;
                }
            }
            if (this.entityWidth <= 1 && var6 != PathType.OPEN && var4.getPathfindingMalus(var6) == 0.0F
                    && this.getPathType(var0, var1, var2, var3) == PathType.OPEN) {
                return PathType.OPEN;
            } else {
                return var6;
            }
        }
    }

    public Set getPathTypeWithinMobBB(PathfindingContext var0, int var1, int var2, int var3) {
        EnumSet var4 = EnumSet.noneOf(PathType.class);

        for (int var5 = 0; var5 < this.entityWidth; ++var5) {
            for (int var6 = 0; var6 < this.entityHeight; ++var6) {
                for (int var7 = 0; var7 < this.entityDepth; ++var7) {
                    int var8 = var5 + var1;
                    int var9 = var6 + var2;
                    int var10 = var7 + var3;
                    PathType var11 = this.getPathType(var0, var8, var9, var10);
                    BlockPos var12 = this.mob.blockPosition();
                    boolean var13 = this.canPassDoors();
                    if (var11 == PathType.DOOR_WOOD_CLOSED && this.canOpenDoors() && var13) {
                        var11 = PathType.WALKABLE_DOOR;
                    }
                    if (var11 == PathType.DOOR_OPEN && !var13) {
                        var11 = PathType.BLOCKED;
                    }
                    if (var11 == PathType.RAIL
                            && this.getPathType(var0, var12.getX(), var12.getY(), var12.getZ()) != PathType.RAIL
                            && this.getPathType(var0, var12.getX(), var12.getY() - 1, var12.getZ()) != PathType.RAIL) {
                        var11 = PathType.UNPASSABLE_RAIL;
                    }
                    var4.add(var11);
                }
            }
        }
        return var4;
    }

    @Override
    public Node getStart() {
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
        int var0 = this.mob.getBlockY();
        BlockState var2 = this.currentContext.getBlockState(var1.set(this.mob.getX(), var0, this.mob.getZ()));
        if (!this.mob.canStandOnFluid(var2.getFluidState())) {
            if (this.canFloat() && this.mob.isInWater()) {
                while (true) {
                    if (!var2.is(Blocks.WATER) && var2.getFluidState() != Fluids.WATER.getSource(false)) {
                        --var0;
                        break;
                    }
                    ++var0;
                    var2 = this.currentContext.getBlockState(var1.set(this.mob.getX(), var0, this.mob.getZ()));
                }
            } else if (this.mob.onGround()) {
                var0 = Mth.floor(this.mob.getY() + 0.5);
            } else {
                var1.set(this.mob.getX(), this.mob.getY() + 1.0, this.mob.getZ());

                while (var1.getY() > this.currentContext.level().getMinBuildHeight()) {
                    var0 = var1.getY();
                    var1.setY(var1.getY() - 1);
                    BlockState var3 = this.currentContext.getBlockState(var1);
                    if (!var3.isAir() && !var3.isPathfindable(PathComputationType.LAND)) {
                        break;
                    }
                }
            }
        } else {
            while (true) {
                if (!this.mob.canStandOnFluid(var2.getFluidState())) {
                    --var0;
                    break;
                }
                ++var0;
                var2 = this.currentContext.getBlockState(var1.set(this.mob.getX(), var0, this.mob.getZ()));
            }
        }
        BlockPos var3 = this.mob.blockPosition();
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
        var1.type = this.getCachedPathType(var1.x, var1.y, var1.z);
        var1.costMalus = this.mvmt.getPathfindingMalus(var1.type);
        return var1;
    }

    @Override
    public Target getTarget(double var0, double var2, double var4) {
        return this.getTargetNodeAt(var0, var2, var4);
    }

    private boolean hasCollisions(AABB var0) {
        return this.collisionCache.computeIfAbsent(var0, (var1) -> {
            return !this.currentContext.level().noCollision(this.mob, var0);
        });
    }

    protected boolean isAmphibious() {
        return false;
    }

    protected boolean isDiagonalValid(Node var0) {
        if (var0 != null && !var0.closed) {
            if (var0.type == PathType.WALKABLE_DOOR) {
                return false;
            } else {
                return var0.costMalus >= 0.0F;
            }
        } else {
            return false;
        }
    }

    protected boolean isDiagonalValid(Node var0, Node var1, Node var2) {
        if (var2 != null && var1 != null && var2.y <= var0.y && var1.y <= var0.y) {
            if (var1.type != PathType.WALKABLE_DOOR && var2.type != PathType.WALKABLE_DOOR) {
                boolean var3 = var2.type == PathType.FENCE && var1.type == PathType.FENCE
                        && this.mob.getBbWidth() < 0.5;
                return (var2.y < var0.y || var2.costMalus >= 0.0F || var3)
                        && (var1.y < var0.y || var1.costMalus >= 0.0F || var3);
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
        this.oldWaterCost = mvmt.getPathfindingMalus(PathType.WATER);
    }

    @Override
    public void prepare(PathNavigationRegion var0, Mob var1) {
        super.prepare(var0, var1);
        this.oldWaterCost = mvmt.getPathfindingMalus(PathType.WATER);
    }

    private Node tryFindFirstGroundNodeBelow(int var0, int var1, int var2) {
        for (int var3 = var1 - 1; var3 >= this.mob.level().getMinBuildHeight(); --var3) {
            if (var1 - var3 > this.mob.getMaxFallDistance()) {
                return this.getBlockedNode(var0, var3, var2);
            }
            PathType var4 = this.getCachedPathType(var0, var3, var2);
            float var5 = this.mvmt.getPathfindingMalus(var4);
            if (var4 != PathType.OPEN) {
                if (var5 >= 0.0F) {
                    return this.getNodeAndUpdateCostToMax(var0, var3, var2, var4, var5);
                }
                return this.getBlockedNode(var0, var3, var2);
            }
        }
        return this.getBlockedNode(var0, var1, var2);
    }

    private Node tryFindFirstNonWaterBelow(int var0, int var1, int var2, Node var3) {
        --var1;

        while (var1 > this.mob.level().getMinBuildHeight()) {
            PathType var4 = this.getCachedPathType(var0, var1, var2);
            if (var4 != PathType.WATER) {
                return var3;
            }
            var3 = this.getNodeAndUpdateCostToMax(var0, var1, var2, var4, this.mvmt.getPathfindingMalus(var4));
            --var1;
        }
        return var3;
    }

    private Node tryJumpOn(int var0, int var1, int var2, int var3, double var4, Direction var6, PathType var7,
            BlockPos.MutableBlockPos var8) {
        Node var9 = this.findAcceptedNode(var0, var1 + 1, var2, var3 - 1, var4, var6, var7);
        if (var9 == null) {
            return null;
        } else if (this.mob.getBbWidth() >= 1.0F) {
            return var9;
        } else if (var9.type != PathType.OPEN && var9.type != PathType.WALKABLE) {
            return var9;
        } else {
            double var10 = var0 - var6.getStepX() + 0.5;
            double var12 = var2 - var6.getStepZ() + 0.5;
            double var14 = this.mob.getBbWidth() / 2.0;
            AABB var16 = new AABB(var10 - var14, this.getFloorLevel(var8.set(var10, var1 + 1, var12)) + 0.001,
                    var12 - var14, var10 + var14,
                    this.mob.getBbHeight()
                            + this.getFloorLevel(var8.set((double) var9.x, (double) var9.y, (double) var9.z)) - 0.002,
                    var12 + var14);
            return this.hasCollisions(var16) ? null : var9;
        }
    }

    public static PathType checkNeighbourBlocks(PathfindingContext var0, int var1, int var2, int var3, PathType var4) {
        for (int var5 = -1; var5 <= 1; ++var5) {
            for (int var6 = -1; var6 <= 1; ++var6) {
                for (int var7 = -1; var7 <= 1; ++var7) {
                    if (var5 != 0 || var7 != 0) {
                        PathType var8 = var0.getPathTypeFromState(var1 + var5, var2 + var6, var3 + var7);
                        if (var8 == PathType.DAMAGE_OTHER) {
                            return PathType.DANGER_OTHER;
                        }
                        if (var8 == PathType.DAMAGE_FIRE || var8 == PathType.LAVA) {
                            return PathType.DANGER_FIRE;
                        }
                        if (var8 == PathType.WATER) {
                            return PathType.WATER_BORDER;
                        }
                        if (var8 == PathType.DAMAGE_CAUTIOUS) {
                            return PathType.DAMAGE_CAUTIOUS;
                        }
                    }
                }
            }
        }
        return var4;
    }

    private static boolean doesBlockHavePartialCollision(PathType var0) {
        return var0 == PathType.FENCE || var0 == PathType.DOOR_WOOD_CLOSED || var0 == PathType.DOOR_IRON_CLOSED;
    }

    public static double getFloorLevel(BlockGetter var0, BlockPos var1) {
        BlockPos var2 = var1.below();
        VoxelShape var3 = var0.getBlockState(var2).getCollisionShape(var0, var2);
        return var2.getY() + (var3.isEmpty() ? 0.0 : var3.max(Axis.Y));
    }

    public static PathType getPathTypeStatic(PathfindingContext var0, BlockPos.MutableBlockPos var1) {
        int var2 = var1.getX();
        int var3 = var1.getY();
        int var4 = var1.getZ();
        PathType var5 = var0.getPathTypeFromState(var2, var3, var4);
        if (var5 == PathType.OPEN && var3 >= var0.level().getMinBuildHeight() + 1) {
            PathType var10000;
            switch (var0.getPathTypeFromState(var2, var3 - 1, var4)) {
                case OPEN:
                case WATER:
                case LAVA:
                case WALKABLE:
                    var10000 = PathType.OPEN;
                    break;
                case DAMAGE_FIRE:
                    var10000 = PathType.DAMAGE_FIRE;
                    break;
                case DAMAGE_OTHER:
                    var10000 = PathType.DAMAGE_OTHER;
                    break;
                case STICKY_HONEY:
                    var10000 = PathType.STICKY_HONEY;
                    break;
                case POWDER_SNOW:
                    var10000 = PathType.DANGER_POWDER_SNOW;
                    break;
                case DAMAGE_CAUTIOUS:
                    var10000 = PathType.DAMAGE_CAUTIOUS;
                    break;
                case TRAPDOOR:
                    var10000 = PathType.DANGER_TRAPDOOR;
                    break;
                default:
                    var10000 = checkNeighbourBlocks(var0, var2, var3, var4, PathType.WALKABLE);
            }
            return var10000;
        } else {
            return var5;
        }
    }

    public static final double SPACE_BETWEEN_WALL_POSTS = 0.5;
}
