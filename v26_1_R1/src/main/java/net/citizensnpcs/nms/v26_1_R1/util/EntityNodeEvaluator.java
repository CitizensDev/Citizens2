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

    private boolean canReachWithoutCollision(Node posTo) {
        AABB bb = this.mob.getBoundingBox();
        Vec3 delta = new Vec3(posTo.x - this.mob.getX() + bb.getXsize() / 2.0,
                posTo.y - this.mob.getY() + bb.getYsize() / 2.0, posTo.z - this.mob.getZ() + bb.getZsize() / 2.0);
        int steps = Mth.ceil(delta.length() / bb.getSize());
        delta = delta.scale(1.0F / steps);

        for (int i = 1; i <= steps; ++i) {
            bb = bb.move(delta);
            if (this.hasCollisions(bb)) {
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

    private Node getBlockedNode(int x, int y, int z) {
        Node node = this.getNode(x, y, z);
        node.type = PathType.BLOCKED;
        node.costMalus = -1.0F;
        return node;
    }

    protected PathType getCachedPathType(int x, int y, int z) {
        return (PathType) this.pathTypesByPosCacheByMob.computeIfAbsent(BlockPos.asLong(x, y, z), (k) -> {
            return this.getPathTypeOfMob(this.currentContext, x, y, z, this.mob);
        });
    }

    private Node getClosedNode(int x, int y, int z, PathType pathType) {
        Node node = this.getNode(x, y, z);
        node.closed = true;
        node.type = pathType;
        node.costMalus = pathType.getMalus();
        return node;
    }

    protected double getFloorLevel(BlockPos pos) {
        BlockGetter level = this.currentContext.level();
        return (this.canFloat() || this.isAmphibious()) && level.getFluidState(pos).is(FluidTags.WATER)
                ? pos.getY() + 0.5
                : getFloorLevel(level, pos);
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

    private Node getNodeAndUpdateCostToMax(int x, int y, int z, PathType pathType, float cost) {
        Node node = this.getNode(x, y, z);
        node.type = pathType;
        node.costMalus = Math.max(node.costMalus, cost);
        return node;
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
    public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
        Set<PathType> blockTypes = this.getPathTypeWithinMobBB(context, x, y, z);
        if (blockTypes.size() == 1) {
            return blockTypes.iterator().next();
        } else if (blockTypes.contains(PathType.FENCE)) {
            return PathType.FENCE;
        } else if (blockTypes.contains(PathType.UNPASSABLE_RAIL)) {
            return PathType.UNPASSABLE_RAIL;
        } else {
            PathType highestMalusPathTypeWithinBB = PathType.BLOCKED;
            float highestMalusWithinBB = mob.getPathfindingMalus(highestMalusPathTypeWithinBB);
            Iterator var9 = blockTypes.iterator();

            while (var9.hasNext()) {
                PathType pathType = (PathType) var9.next();
                float malusForPathType = mob.getPathfindingMalus(pathType);
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
                boolean isCurrentNodeCheaper = mob.getPathfindingMalus(currentNodePathType) < highestMalusWithinBB;
                boolean capMalusDueToCheapNode = isCurrentNodeCheaper
                        && mob.getPathfindingMalus(PathType.BIG_MOBS_CLOSE_TO_DANGER) < highestMalusWithinBB;
                return capMalusDueToCheapNode ? PathType.BIG_MOBS_CLOSE_TO_DANGER : highestMalusPathTypeWithinBB;
            } else {
                return currentNodePathType == PathType.OPEN && highestMalusPathTypeWithinBB != PathType.OPEN
                        && highestMalusWithinBB == 0.0F ? PathType.OPEN : highestMalusPathTypeWithinBB;
            }
        }
    }

    public Set<PathType> getPathTypeWithinMobBB(PathfindingContext context, int x, int y, int z) {
        EnumSet<PathType> blockTypes = EnumSet.noneOf(PathType.class);

        for (int dx = 0; dx < this.entityWidth; ++dx) {
            for (int dy = 0; dy < this.entityHeight; ++dy) {
                for (int dz = 0; dz < this.entityDepth; ++dz) {
                    int xx = dx + x;
                    int yy = dy + y;
                    int zz = dz + z;
                    PathType blockType = this.getPathType(context, xx, yy, zz);
                    BlockPos mobPosition = this.mob.blockPosition();
                    boolean canPassDoors = this.canPassDoors();
                    if (blockType == PathType.DOOR_WOOD_CLOSED && this.canOpenDoors() && canPassDoors) {
                        blockType = PathType.WALKABLE_DOOR;
                    }
                    if (blockType == PathType.DOOR_OPEN && !canPassDoors) {
                        blockType = PathType.BLOCKED;
                    }
                    if (blockType == PathType.RAIL
                            && this.getPathType(context, mobPosition.getX(), mobPosition.getY(),
                                    mobPosition.getZ()) != PathType.RAIL
                            && this.getPathType(context, mobPosition.getX(), mobPosition.getY() - 1,
                                    mobPosition.getZ()) != PathType.RAIL) {
                        blockType = PathType.UNPASSABLE_RAIL;
                    }
                    blockTypes.add(blockType);
                }
            }
        }
        return blockTypes;
    }

    @Override
    public Node getStart() {
        BlockPos.MutableBlockPos reusablePos = new BlockPos.MutableBlockPos();
        int startY = this.mob.getBlockY();
        BlockState blockState = this.currentContext
                .getBlockState(reusablePos.set(this.mob.getX(), startY, this.mob.getZ()));
        PathfindingContext var10000;
        double var10002;
        if (!this.mob.canStandOnFluid(blockState.getFluidState())) {
            if (this.canFloat() && this.mob.isInWater()) {
                while (true) {
                    if (!blockState.is(Blocks.WATER) && blockState.getFluidState() != Fluids.WATER.getSource(false)) {
                        --startY;
                        break;
                    }
                    var10000 = this.currentContext;
                    var10002 = this.mob.getX();
                    ++startY;
                    blockState = var10000.getBlockState(reusablePos.set(var10002, startY, this.mob.getZ()));
                }
            } else if (this.mob.onGround()) {
                startY = Mth.floor(this.mob.getY() + 0.5);
            } else {
                reusablePos.set(this.mob.getX(), this.mob.getY() + 1.0, this.mob.getZ());

                while (reusablePos.getY() > this.currentContext.level().getMinY()) {
                    startY = reusablePos.getY();
                    reusablePos.setY(reusablePos.getY() - 1);
                    BlockState belowBlockState = this.currentContext.getBlockState(reusablePos);
                    if (!belowBlockState.isAir() && !belowBlockState.isPathfindable(PathComputationType.LAND)) {
                        break;
                    }
                }
            }
        } else {
            while (true) {
                if (!this.mob.canStandOnFluid(blockState.getFluidState())) {
                    --startY;
                    break;
                }
                var10000 = this.currentContext;
                var10002 = this.mob.getX();
                ++startY;
                blockState = var10000.getBlockState(reusablePos.set(var10002, startY, this.mob.getZ()));
            }
        }
        BlockPos startPos = this.mob.blockPosition();
        if (!this.canStartAt(reusablePos.set(startPos.getX(), startY, startPos.getZ()))) {
            AABB mobBB = this.mob.getBoundingBox();
            if (this.canStartAt(reusablePos.set(mobBB.minX, startY, mobBB.minZ))
                    || this.canStartAt(reusablePos.set(mobBB.minX, startY, mobBB.maxZ))
                    || this.canStartAt(reusablePos.set(mobBB.maxX, startY, mobBB.minZ))
                    || this.canStartAt(reusablePos.set(mobBB.maxX, startY, mobBB.maxZ))) {
                return this.getStartNode(reusablePos);
            }
        }
        return this.getStartNode(new BlockPos(startPos.getX(), startY, startPos.getZ()));
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

    private boolean hasCollisions(AABB aabb) {
        return this.collisionCache.computeIfAbsent(aabb, (bb) -> {
            return !this.currentContext.level().noCollision(this.mob, aabb);
        });
    }

    protected boolean isAmphibious() {
        return false;
    }

    protected boolean isDiagonalValid(Node diagonal) {
        return diagonal != null && !diagonal.closed && diagonal.type != PathType.WALKABLE_DOOR
                && diagonal.costMalus >= 0.0F;
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

    protected boolean isNeighborValid(Node neighbor, Node current) {
        return neighbor != null && !neighbor.closed && (neighbor.costMalus >= 0.0F || current.costMalus < 0.0F);
    }

    @Override
    public void prepare(PathNavigationRegion var0, LivingEntity var1) {
        super.prepare(var0, var1);
        this.oldWaterCost = mvmt.getPathfindingMalus(PathType.WATER);
    }

    @Override
    public void prepare(PathNavigationRegion var0, Mob var1) {
        super.prepare(var0, var1);
        var1.onPathfindingStart();
        this.oldWaterCost = mvmt.getPathfindingMalus(PathType.WATER);
    }

    private Node tryFindFirstGroundNodeBelow(int x, int y, int z) {
        for (int currentY = y - 1; currentY >= this.mob.level().getMinY(); --currentY) {
            if (y - currentY > this.mob.getMaxFallDistance()) {
                return this.getBlockedNode(x, currentY, z);
            }
            PathType pathType = this.getCachedPathType(x, currentY, z);
            float pathCost = this.mvmt.getPathfindingMalus(pathType);
            if (pathType != PathType.OPEN) {
                if (pathCost >= 0.0F) {
                    return this.getNodeAndUpdateCostToMax(x, currentY, z, pathType, pathCost);
                }
                return this.getBlockedNode(x, currentY, z);
            }
        }
        return this.getBlockedNode(x, y, z);
    }

    private Node tryFindFirstNonWaterBelow(int x, int y, int z, Node best) {
        --y;

        while (y > this.mob.level().getMinY()) {
            PathType pathTypeLocal = this.getCachedPathType(x, y, z);
            if (pathTypeLocal != PathType.WATER) {
                return best;
            }
            best = this.getNodeAndUpdateCostToMax(x, y, z, pathTypeLocal, this.mvmt.getPathfindingMalus(pathTypeLocal));
            --y;
        }
        return best;
    }

    private Node tryJumpOn(int x, int y, int z, int jumpSize, double nodeHeight, Direction travelDirection,
            PathType blockPathTypeCurrent, BlockPos.MutableBlockPos reusablePos) {
        Node nodeAbove = this.findAcceptedNode(x, y + 1, z, jumpSize - 1, nodeHeight, travelDirection,
                blockPathTypeCurrent);
        if (nodeAbove == null) {
            return null;
        } else if (this.mob.getBbWidth() >= 1.0F) {
            return nodeAbove;
        } else if (nodeAbove.type != PathType.OPEN && nodeAbove.type != PathType.WALKABLE) {
            return nodeAbove;
        } else {
            double centerX = x - travelDirection.getStepX() + 0.5;
            double centerZ = z - travelDirection.getStepZ() + 0.5;
            double halfWidth = this.mob.getBbWidth() / 2.0;
            AABB grow = new AABB(centerX - halfWidth,
                    this.getFloorLevel(reusablePos.set(centerX, y + 1, centerZ)) + 0.001, centerZ - halfWidth,
                    centerX + halfWidth,
                    this.mob.getBbHeight()
                            + this.getFloorLevel(
                                    reusablePos.set((double) nodeAbove.x, (double) nodeAbove.y, (double) nodeAbove.z))
                            - 0.002,
                    centerZ + halfWidth);
            return this.hasCollisions(grow) ? null : nodeAbove;
        }
    }

    public static PathType checkNeighbourBlocks(PathfindingContext context, int x, int y, int z,
            PathType blockPathType) {
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dy = -1; dy <= 1; ++dy) {
                for (int dz = -1; dz <= 1; ++dz) {
                    if (dx != 0 || dz != 0) {
                        PathType pathType = context.getPathTypeFromState(x + dx, y + dy, z + dz);
                        if (pathType == PathType.DAMAGING) {
                            return PathType.DAMAGING_IN_NEIGHBOR;
                        }
                        if (pathType == PathType.FIRE || pathType == PathType.LAVA) {
                            return PathType.FIRE_IN_NEIGHBOR;
                        }
                        if (pathType == PathType.WATER) {
                            return PathType.WATER_BORDER;
                        }
                        if (pathType == PathType.DAMAGE_CAUTIOUS) {
                            return PathType.DAMAGE_CAUTIOUS;
                        }
                    }
                }
            }
        }
        return blockPathType;
    }

    private static boolean doesBlockHavePartialCollision(PathType type) {
        return type == PathType.FENCE || type == PathType.DOOR_WOOD_CLOSED || type == PathType.DOOR_IRON_CLOSED;
    }

    public static double getFloorLevel(BlockGetter level, BlockPos pos) {
        BlockPos target = pos.below();
        VoxelShape shape = level.getBlockState(target).getCollisionShape(level, target);
        return target.getY() + (shape.isEmpty() ? 0.0 : shape.max(Axis.Y));
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
