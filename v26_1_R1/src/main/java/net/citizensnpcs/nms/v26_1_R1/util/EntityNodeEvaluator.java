package net.citizensnpcs.nms.v26_1_R1.util;

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
            if (this.hasCollisions(var1))
                return false;
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

    protected Node findAcceptedNode(int x, int y, int z, int jumpSize, double nodeHeight, Direction travelDirection,
            PathType blockPathTypeCurrent) {
        Node best = null;
        BlockPos.MutableBlockPos reusablePos = new BlockPos.MutableBlockPos();
        double maxYTarget = this.getFloorLevel(reusablePos.set(x, y, z));
        if (maxYTarget - nodeHeight > this.getMobJumpHeight())
            return null;
        else {
            PathType pathType = this.getCachedPathType(x, y, z);
            float pathCost = this.mvmt.getPathfindingMalus(pathType);
            if (pathCost >= 0.0F) {
                best = this.getNodeAndUpdateCostToMax(x, y, z, pathType, pathCost);
            }
            if (doesBlockHavePartialCollision(blockPathTypeCurrent) && best != null && best.costMalus >= 0.0F
                    && !this.canReachWithoutCollision(best)) {
                best = null;
            }
            if (pathType != PathType.WALKABLE && (!this.isAmphibious() || pathType != PathType.WATER)) {
                if ((best == null || best.costMalus < 0.0F) && jumpSize > 0
                        && (pathType != PathType.FENCE || this.canWalkOverFences())
                        && pathType != PathType.UNPASSABLE_RAIL && pathType != PathType.TRAPDOOR
                        && pathType != PathType.POWDER_SNOW) {
                    best = this.tryJumpOn(x, y, z, jumpSize, nodeHeight, travelDirection, blockPathTypeCurrent,
                            reusablePos);
                } else if (!this.isAmphibious() && pathType == PathType.WATER && !this.canFloat()) {
                    best = this.tryFindFirstNonWaterBelow(x, y, z, best);
                } else if (pathType == PathType.OPEN) {
                    best = this.tryFindFirstGroundNodeBelow(x, y, z);
                } else if (doesBlockHavePartialCollision(pathType) && best == null) {
                    best = this.getClosedNode(x, y, z, pathType);
                }
            }
            return best;
        }
    }

    private Node getBlockedNode(int var0, int var1, int var2) {
        Node var3 = this.getNode(var0, var1, var2);
        var3.type = PathType.BLOCKED;
        var3.costMalus = -1.0F;
        return var3;
    }

    protected PathType getCachedPathType(int var0, int var1, int var2) {
        return (PathType) this.pathTypesByPosCacheByMob.computeIfAbsent(BlockPos.asLong(var0, var1, var2),
                var3 -> this.getPathTypeOfMob(this.currentContext, var0, var1, var2, this.mob));
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
    public int getNeighbors(Node[] neighbors, Node pos) {
        int p = 0;
        int jumpSize = 0;
        PathType blockPathTypeAbove = this.getCachedPathType(pos.x, pos.y + 1, pos.z);
        PathType blockPathTypeCurrent = this.getCachedPathType(pos.x, pos.y, pos.z);
        if (this.mvmt.getPathfindingMalus(blockPathTypeAbove) >= 0.0F
                && blockPathTypeCurrent != PathType.STICKY_HONEY) {
            jumpSize = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
        }
        double posHeight = this.getFloorLevel(new BlockPos(pos.x, pos.y, pos.z));
        Iterator var9 = Plane.HORIZONTAL.iterator();

        Direction directionx;
        while (var9.hasNext()) {
            directionx = (Direction) var9.next();
            Node node = this.findAcceptedNode(pos.x + directionx.getStepX(), pos.y, pos.z + directionx.getStepZ(),
                    jumpSize, posHeight, directionx, blockPathTypeCurrent);
            this.reusableNeighbors[directionx.get2DDataValue()] = node;
            if (this.isNeighborValid(node, pos)) {
                neighbors[p++] = node;
            }
        }
        var9 = Plane.HORIZONTAL.iterator();

        while (var9.hasNext()) {
            directionx = (Direction) var9.next();
            Direction secondDirection = directionx.getClockWise();
            if (this.isDiagonalValid(pos, this.reusableNeighbors[directionx.get2DDataValue()],
                    this.reusableNeighbors[secondDirection.get2DDataValue()])) {
                Node diagonalNode = this.findAcceptedNode(pos.x + directionx.getStepX() + secondDirection.getStepX(),
                        pos.y, pos.z + directionx.getStepZ() + secondDirection.getStepZ(), jumpSize, posHeight,
                        directionx, blockPathTypeCurrent);
                if (this.isDiagonalValid(diagonalNode)) {
                    neighbors[p++] = diagonalNode;
                }
            }
        }
        return p;
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

    public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, LivingEntity var4) {
        Set<PathType> blockTypes = this.getPathTypeWithinMobBB(context, x, y, z);
        if (blockTypes.size() == 1) {
            return blockTypes.iterator().next();
        } else if (blockTypes.contains(PathType.FENCE)) {
            return PathType.FENCE;
        } else if (blockTypes.contains(PathType.UNPASSABLE_RAIL)) {
            return PathType.UNPASSABLE_RAIL;
        } else {
            PathType highestMalusPathTypeWithinBB = PathType.BLOCKED;
            float highestMalusWithinBB = mvmt.getPathfindingMalus(highestMalusPathTypeWithinBB);
            Iterator var9 = blockTypes.iterator();

            while (var9.hasNext()) {
                PathType pathType = (PathType) var9.next();
                float malusForPathType = mvmt.getPathfindingMalus(pathType);
                if (malusForPathType < 0.0F) {
                    return pathType;
                }
                if (malusForPathType >= highestMalusWithinBB) {
                    highestMalusWithinBB = malusForPathType;
                    highestMalusPathTypeWithinBB = pathType;
                }
            }
            PathType currentNodePathType = this.getPathType(context, x, y, z);
            boolean isLargeMob = this.entityWidth > 1;
            if (isLargeMob) {
                boolean isCurrentNodeCheaper = mvmt.getPathfindingMalus(currentNodePathType) < highestMalusWithinBB;
                boolean capMalusDueToCheapNode = isCurrentNodeCheaper
                        && mvmt.getPathfindingMalus(PathType.BIG_MOBS_CLOSE_TO_DANGER) < highestMalusWithinBB;
                return capMalusDueToCheapNode ? PathType.BIG_MOBS_CLOSE_TO_DANGER : highestMalusPathTypeWithinBB;
            } else {
                return currentNodePathType == PathType.OPEN && highestMalusPathTypeWithinBB != PathType.OPEN
                        && highestMalusWithinBB == 0.0F ? PathType.OPEN : highestMalusPathTypeWithinBB;
            }
        }
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext var0, int var1, int var2, int var3, Mob var4) {
        Set var5 = this.getPathTypeWithinMobBB(var0, var1, var2, var3);
        if (var5.contains(PathType.FENCE))
            return PathType.FENCE;
        else if (var5.contains(PathType.UNPASSABLE_RAIL))
            return PathType.UNPASSABLE_RAIL;
        else {
            PathType var6 = PathType.BLOCKED;
            Iterator<PathType> var88 = var5.iterator();

            while (var88.hasNext()) {
                PathType var8 = var88.next();
                if (var4.getPathfindingMalus(var8) < 0.0F)
                    return var8;
                if (var4.getPathfindingMalus(var8) >= var4.getPathfindingMalus(var6)) {
                    var6 = var8;
                }
            }
            if (this.entityWidth <= 1 && var6 != PathType.OPEN && var4.getPathfindingMalus(var6) == 0.0F
                    && this.getPathType(var0, var1, var2, var3) == PathType.OPEN)
                return PathType.OPEN;
            else
                return var6;
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

                while (var1.getY() > this.currentContext.level().getMinY()) {
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
                    || this.canStartAt(var1.set(var4.maxX, var0, var4.maxZ)))
                return this.getStartNode(var1);
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
        return this.collisionCache.computeIfAbsent(var0,
                var1 -> !this.currentContext.level().noCollision(this.mob, var0));
    }

    protected boolean isAmphibious() {
        return false;
    }

    protected boolean isDiagonalValid(Node var0) {
        if (var0 == null || var0.closed || var0.type == PathType.WALKABLE_DOOR)
            return false;
        else
            return var0.costMalus >= 0.0F;
    }

    protected boolean isDiagonalValid(Node pos, Node ew, Node ns) {
        if (ns != null && ew != null && ns.y <= pos.y && ew.y <= pos.y) {
            if (ew.type != PathType.WALKABLE_DOOR && ns.type != PathType.WALKABLE_DOOR) {
                if (this.mob.getBbWidth() > 1.0F && (ew.costMalus > 0.0F || ns.costMalus > 0.0F)) {
                    return false;
                } else {
                    boolean canPassBetweenPosts = ns.type == PathType.FENCE && ew.type == PathType.FENCE
                            && this.mob.getBbWidth() < 0.5;
                    return (ns.y < pos.y || ns.costMalus >= 0.0F || canPassBetweenPosts)
                            && (ew.y < pos.y || ew.costMalus >= 0.0F || canPassBetweenPosts);
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
        this.oldWaterCost = mvmt.getPathfindingMalus(PathType.WATER);
    }

    @Override
    public void prepare(PathNavigationRegion var0, Mob var1) {
        super.prepare(var0, var1);
        this.oldWaterCost = mvmt.getPathfindingMalus(PathType.WATER);
    }

    private Node tryFindFirstGroundNodeBelow(int var0, int var1, int var2) {
        for (int var3 = var1 - 1; var3 >= this.mob.level().getMinY(); --var3) {
            if (var1 - var3 > this.mob.getMaxFallDistance())
                return this.getBlockedNode(var0, var3, var2);
            PathType var4 = this.getCachedPathType(var0, var3, var2);
            float var5 = this.mvmt.getPathfindingMalus(var4);
            if (var4 != PathType.OPEN) {
                if (var5 >= 0.0F)
                    return this.getNodeAndUpdateCostToMax(var0, var3, var2, var4, var5);
                return this.getBlockedNode(var0, var3, var2);
            }
        }
        return this.getBlockedNode(var0, var1, var2);
    }

    private Node tryFindFirstNonWaterBelow(int var0, int var1, int var2, Node var3) {
        --var1;

        while (var1 > this.mob.level().getMinY()) {
            PathType var4 = this.getCachedPathType(var0, var1, var2);
            if (var4 != PathType.WATER)
                return var3;
            var3 = this.getNodeAndUpdateCostToMax(var0, var1, var2, var4, this.mvmt.getPathfindingMalus(var4));
            --var1;
        }
        return var3;
    }

    private Node tryJumpOn(int var0, int var1, int var2, int var3, double var4, Direction var6, PathType var7,
            BlockPos.MutableBlockPos var8) {
        Node var9 = this.findAcceptedNode(var0, var1 + 1, var2, var3 - 1, var4, var6, var7);
        if (var9 == null)
            return null;
        else if (this.mob.getBbWidth() >= 1.0F || var9.type != PathType.OPEN && var9.type != PathType.WALKABLE)
            return var9;
        else {
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
                        if (var8 == PathType.FIRE || var8 == PathType.LAVA)
                            return PathType.FIRE;
                        if (var8 == PathType.WATER)
                            return PathType.WATER_BORDER;
                        if (var8 == PathType.DAMAGE_CAUTIOUS)
                            return PathType.DAMAGE_CAUTIOUS;
                    }
                }
            }
        }
        return var4;
    }

    private static boolean doesBlockHavePartialCollision(PathType type) {
        return type == PathType.FENCE || type == PathType.DOOR_WOOD_CLOSED || type == PathType.DOOR_IRON_CLOSED;
    }

    public static double getFloorLevel(BlockGetter var0, BlockPos var1) {
        BlockPos var2 = var1.below();
        VoxelShape var3 = var0.getBlockState(var2).getCollisionShape(var0, var2);
        return var2.getY() + (var3.isEmpty() ? 0.0 : var3.max(Axis.Y));
    }

    public static PathType getPathTypeStatic(PathfindingContext context, BlockPos.MutableBlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        PathType blockPathType = context.getPathTypeFromState(x, y, z);
        if (blockPathType == PathType.OPEN && y >= context.level().getMinY() + 1) {
            PathType var10000;
            switch (context.getPathTypeFromState(x, y - 1, z)) {
                case OPEN:
                case WATER:
                case LAVA:
                case WALKABLE:
                    var10000 = PathType.OPEN;
                    break;
                case FIRE:
                    var10000 = PathType.FIRE;
                    break;
                case DAMAGING:
                    var10000 = PathType.DAMAGING;
                    break;
                case STICKY_HONEY:
                    var10000 = PathType.STICKY_HONEY;
                    break;
                case POWDER_SNOW:
                    var10000 = PathType.ON_TOP_OF_POWDER_SNOW;
                    break;
                case DAMAGE_CAUTIOUS:
                    var10000 = PathType.DAMAGE_CAUTIOUS;
                    break;
                case TRAPDOOR:
                    var10000 = PathType.ON_TOP_OF_TRAPDOOR;
                    break;
                default:
                    var10000 = checkNeighbourBlocks(context, x, y, z, PathType.WALKABLE);
            }
            return var10000;
        } else {
            return blockPathType;
        }
    }

    public static final double SPACE_BETWEEN_WALL_POSTS = 0.5;
}
