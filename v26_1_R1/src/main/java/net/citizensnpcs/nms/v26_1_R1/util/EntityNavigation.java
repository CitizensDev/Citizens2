package net.citizensnpcs.nms.v26_1_R1.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import net.citizensnpcs.Settings.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class EntityNavigation extends PathNavigation {
    private boolean avoidSun;
    private boolean canPathToTargetsBelowSurface;
    private final AttributeInstance followRange;
    protected boolean hasDelayedRecomputation;
    private boolean isStuck;
    protected int lastStuckCheck;
    protected Vec3 lastStuckCheckPos = Vec3.ZERO;
    protected long lastTimeoutCheck;
    protected float maxDistanceToWaypoint = 0.5F;
    private float maxVisitedNodesMultiplier = 1.0F;
    private final LivingEntity mob;
    private final MobAI mvmt;
    protected EntityNodeEvaluator nodeEvaluator;
    protected Path path;
    private final EntityPathfinder pathFinder;
    private int reachRange;
    private float requiredPathLength = 16.0F;
    protected double speedModifier;
    private BlockPos targetPos;
    protected int tick;
    protected long timeLastRecompute;
    protected Vec3i timeoutCachedNode = Vec3i.ZERO;
    protected double timeoutLimit;
    protected long timeoutTimer;

    public EntityNavigation(LivingEntity entityinsentient, Level world) {
        super(new Slime(EntityType.SLIME, world), world);
        this.mob = entityinsentient;
        this.mvmt = MobAI.from(entityinsentient);
        this.followRange = entityinsentient.getAttribute(Attributes.FOLLOW_RANGE);
        this.nodeEvaluator = new EntityNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        this.pathFinder = new EntityPathfinder(this.nodeEvaluator,
                Setting.MINECRAFT_PATHFINDER_MAXIMUM_VISITED_NODES.asInt());
    }

    @Override
    public boolean canCutCorner(final PathType pathType) {
        return pathType != PathType.FIRE_IN_NEIGHBOR && pathType != PathType.DAMAGING_IN_NEIGHBOR
                && pathType != PathType.WALKABLE_DOOR;
    }

    @Override
    public boolean canFloat() {
        return this.nodeEvaluator.canFloat();
    }

    @Override
    protected boolean canMoveDirectly(Vec3 var0, Vec3 var1) {
        return false;
    }

    @Override
    public boolean canNavigateGround() {
        return true;
    }

    public boolean canOpenDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public boolean canPassDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    @Override
    protected boolean canUpdatePath() {
        return this.mob.onGround() || this.mob.isInLiquid() || this.mob.isPassenger();
    }

    @Override
    public Path createPath(BlockPos pos, final int reachRange) {
        LevelChunk chunk = this.level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()),
                SectionPos.blockToSectionCoord(pos.getZ()));
        if (chunk == null) {
            return null;
        } else {
            if (!this.canPathToTargetsBelowSurface) {
                pos = this.findSurfacePosition(chunk, pos, reachRange);
            }
            return supercreatePath(pos, reachRange);
        }
    }

    @Override
    public Path createPath(BlockPos var0, int var1, int var2) {
        return createPath(ImmutableSet.of(var0), 8, false, var1, var2);
    }

    @Override
    public Path createPath(Entity var0, int var1) {
        return createPath(var0.blockPosition(), var1);
    }

    protected Path createPath(Set<BlockPos> targets, Entity target, int radiusOffset, boolean above, int reachRange,
            float maxPathLength) {
        if (targets.isEmpty()) {
            return null;
        } else if (this.mob.getY() < this.level.getMinY()) {
            return null;
        } else if (!this.canUpdatePath()) {
            return null;
        } else if (this.path != null && !this.path.isDone() && targets.contains(this.targetPos)) {
            return this.path;
        } else {
            boolean copiedSet = false;
            Iterator var8 = targets.iterator();

            do {
                BlockPos possibleTarget;
                do {
                    if (!var8.hasNext()) {
                        ProfilerFiller profiler = Profiler.get();
                        profiler.push("pathfind");
                        possibleTarget = above ? this.mob.blockPosition().above() : this.mob.blockPosition();
                        int radius = (int) (maxPathLength + radiusOffset);
                        PathNavigationRegion region = new PathNavigationRegion(this.level,
                                possibleTarget.offset(-radius, -radius, -radius),
                                possibleTarget.offset(radius, radius, radius));
                        Path path = this.pathFinder.findPath(region, this.mob, targets, maxPathLength, reachRange,
                                this.maxVisitedNodesMultiplier);
                        profiler.pop();
                        if (path != null && path.getTarget() != null) {
                            this.targetPos = path.getTarget();
                            this.reachRange = reachRange;
                            this.resetStuckTimeout();
                        }
                        return path;
                    }
                    possibleTarget = (BlockPos) var8.next();
                } while (this.mob.level().getWorldBorder().isWithinBounds(possibleTarget));
                if (!copiedSet) {
                    copiedSet = true;
                    targets = new HashSet(targets);
                }
                targets.remove(possibleTarget);
            } while (!targets.isEmpty());
            return null;
        }
    }

    @Override
    public Path createPath(Set<BlockPos> var0, int var1) {
        return createPath(var0, 8, false, var1);
    }

    @Override
    protected Path createPath(Set<BlockPos> var0, int var1, boolean var2, int reachRange) {
        return createPath(var0, var1, var2, reachRange, (float) this.followRange.getValue());
    }

    @Override
    protected Path createPath(Set<BlockPos> var0, int var1, boolean headAbove, int reachRange, float range) {
        if (var0.isEmpty() || this.mob.getY() < this.level.getMinY() || !canUpdatePath())
            return null;
        if (this.path != null && !this.path.isDone() && var0.contains(this.targetPos))
            return this.path;
        BlockPos headPos = headAbove ? this.mob.blockPosition().above() : this.mob.blockPosition();
        int blockRange = (int) (range + var1);
        PathNavigationRegion region = new PathNavigationRegion(this.level,
                headPos.offset(-blockRange, -blockRange, -blockRange),
                headPos.offset(blockRange, blockRange, blockRange));
        Path var8 = this.pathFinder.findPath(region, this.mob, var0, range, reachRange, this.maxVisitedNodesMultiplier);
        if (var8 != null && var8.getTarget() != null) {
            this.targetPos = var8.getTarget();
            this.reachRange = reachRange;
            this.resetStuckTimeout();
        }
        return var8;
    }

    @Override
    public Path createPath(Stream<BlockPos> var0, int var1) {
        return createPath(var0.collect(Collectors.<BlockPos> toSet()), 8, false, var1);
    }

    @Override
    protected PathFinder createPathFinder(int paramInt) {
        return new PathFinder(nodeEvaluator, paramInt);
    }

    @Override
    protected void doStuckDetection(final Vec3 mobPos) {
        if (this.tick - this.lastStuckCheck > 100) {
            float effectiveSpeed = this.mob.getSpeed() >= 1.0F ? this.mob.getSpeed()
                    : this.mob.getSpeed() * this.mob.getSpeed();
            float thresholdDistance = effectiveSpeed * 100.0F * 0.25F;
            if (mobPos.distanceToSqr(this.lastStuckCheckPos) < thresholdDistance * thresholdDistance) {
                this.isStuck = true;
                this.stop();
            } else {
                this.isStuck = false;
            }
            this.lastStuckCheck = this.tick;
            this.lastStuckCheckPos = mobPos;
        }
        if (this.path != null && !this.path.isDone()) {
            Vec3i pos = this.path.getNextNodePos();
            long time = this.level.getGameTime();
            if (pos.equals(this.timeoutCachedNode)) {
                this.timeoutTimer += time - this.lastTimeoutCheck;
            } else {
                this.timeoutCachedNode = pos;
                double distToNode = mobPos.distanceTo(Vec3.atBottomCenterOf(this.timeoutCachedNode));
                this.timeoutLimit = this.mob.getSpeed() > 0.0F ? distToNode / this.mob.getSpeed() * 20.0 : 0.0;
            }
            if (this.timeoutLimit > 0.0 && this.timeoutTimer > this.timeoutLimit * 3.0) {
                this.timeoutPath();
            }
            this.lastTimeoutCheck = time;
        }
    }

    final BlockPos findSurfacePosition(final LevelChunk chunk, BlockPos pos, final int reachRange) {
        BlockPos.MutableBlockPos columnPos;
        if (chunk.getBlockState(pos).isAir()) {
            columnPos = pos.mutable().move(Direction.DOWN);

            while (columnPos.getY() >= this.level.getMinY() && chunk.getBlockState(columnPos).isAir()) {
                columnPos.move(Direction.DOWN);
            }
            if (columnPos.getY() >= this.level.getMinY()) {
                return columnPos.above();
            }
            columnPos.setY(pos.getY() + 1);

            while (columnPos.getY() <= this.level.getMaxY() && chunk.getBlockState(columnPos).isAir()) {
                columnPos.move(Direction.UP);
            }
            pos = columnPos;
        }
        if (!chunk.getBlockState(pos).isSolid()) {
            return pos;
        } else {
            columnPos = pos.mutable().move(Direction.UP);

            while (columnPos.getY() <= this.level.getMaxY() && chunk.getBlockState(columnPos).isSolid()) {
                columnPos.move(Direction.UP);
            }
            return columnPos.immutable();
        }
    }

    @Override
    protected void followThePath() {
        Vec3 mobPos = this.getTempMobPos();
        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F
                : 0.75F - this.mob.getBbWidth() / 2.0F;
        Vec3i currentNodePos = this.path.getNextNodePos();
        double xDistance = Math.abs(this.mob.getX() - (currentNodePos.getX() + 0.5));
        double yDistance = Math.abs(this.mob.getY() - currentNodePos.getY());
        double zDistance = Math.abs(this.mob.getZ() - (currentNodePos.getZ() + 0.5));
        boolean isCloseEnoughToCurrentNode = xDistance < this.maxDistanceToWaypoint
                && zDistance < this.maxDistanceToWaypoint && yDistance < 1.0;
        if (isCloseEnoughToCurrentNode
                || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(mobPos)) {
            this.path.advance();
        }
        this.doStuckDetection(mobPos);
    }

    @Override
    protected double getGroundY(Vec3 var0) {
        BlockPos var1 = BlockPos.containing(var0);
        return this.level.getBlockState(var1.below()).isAir() ? var0.y
                : WalkNodeEvaluator.getFloorLevel(this.level, var1);
    }

    @Override
    public float getMaxDistanceToWaypoint() {
        return this.maxDistanceToWaypoint;
    }

    private float getMaxPathLength() {
        return Math.max((float) this.mob.getAttributeValue(Attributes.FOLLOW_RANGE), this.requiredPathLength);
    }

    @Override
    public NodeEvaluator getNodeEvaluator() {
        return this.nodeEvaluator;
    }

    @Override
    public Path getPath() {
        return this.path;
    }

    public Path getPathEntity() {
        return this.path;
    }

    private int getSurfaceY() {
        if (this.mob.isInWater() && this.canFloat()) {
            int surface = this.mob.getBlockY();
            BlockState state = this.level.getBlockState(BlockPos.containing(this.mob.getX(), surface, this.mob.getZ()));
            int steps = 0;

            do {
                if (!state.is(Blocks.WATER)) {
                    return surface;
                }
                ++surface;
                state = this.level.getBlockState(BlockPos.containing(this.mob.getX(), surface, this.mob.getZ()));
                ++steps;
            } while (steps <= 16);
            return this.mob.getBlockY();
        } else {
            return Mth.floor(this.mob.getY() + 0.5);
        }
    }

    @Override
    public BlockPos getTargetPos() {
        return this.targetPos;
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), getSurfaceY(), this.mob.getZ());
    }

    protected boolean hasValidPathType(final PathType pathType) {
        if (pathType == PathType.WATER) {
            return false;
        } else if (pathType == PathType.LAVA) {
            return false;
        } else {
            return pathType != PathType.OPEN;
        }
    }

    @Override
    public boolean isDone() {
        return this.path == null || this.path.isDone();
    }

    @Override
    public boolean isInProgress() {
        return !isDone();
    }

    @Override
    public boolean isStableDestination(BlockPos var0) {
        BlockPos var1 = var0.below();
        return this.level.getBlockState(var1).isSolidRender();
    }

    @Override
    public boolean isStuck() {
        return this.isStuck;
    }

    @Override
    public boolean moveTo(double var0, double var2, double var4, double var6) {
        return moveTo(createPath(BlockPos.containing(var0, var2, var4), 1), var6);
    }

    @Override
    public boolean moveTo(Entity var0, double var1) {
        Path var3 = createPath(var0, 1);
        return var3 != null && moveTo(var3, var1);
    }

    @Override
    public boolean moveTo(final Path newPath, final double speedModifier) {
        if (newPath == null) {
            this.path = null;
            return false;
        }
        if (!newPath.sameAs(this.path)) {
            this.path = newPath;
        }
        if (this.isDone()) {
            return false;
        } else {
            this.trimPath();
            if (this.path.getNodeCount() <= 0) {
                return false;
            } else {
                this.speedModifier = speedModifier;
                Vec3 mobPos = this.getTempMobPos();
                this.lastStuckCheck = this.tick;
                this.lastStuckCheckPos = mobPos;
                return true;
            }
        }
    }

    @Override
    public void recomputePath() {
        if (this.level.getGameTime() - this.timeLastRecompute > 20L && this.canUpdatePath()) {
            if (this.targetPos != null) {
                this.path = null;
                this.path = this.createPath(this.targetPos, this.reachRange);
                this.timeLastRecompute = this.level.getGameTime();
                this.hasDelayedRecomputation = false;
            }
        } else {
            this.hasDelayedRecomputation = true;
        }
    }

    @Override
    public void resetMaxVisitedNodesMultiplier() {
        this.maxVisitedNodesMultiplier = 1.0F;
    }

    private void resetStuckTimeout() {
        this.timeoutCachedNode = Vec3i.ZERO;
        this.timeoutTimer = 0L;
        this.timeoutLimit = 0.0D;
        this.isStuck = false;
    }

    public void setAvoidSun(boolean var0) {
        this.avoidSun = var0;
    }

    @Override
    public void setCanFloat(boolean var0) {
        this.nodeEvaluator.setCanFloat(var0);
    }

    @Override
    public void setCanOpenDoors(boolean var0) {
        this.nodeEvaluator.setCanOpenDoors(var0);
    }

    public void setCanPassDoors(boolean var0) {
        this.nodeEvaluator.setCanPassDoors(var0);
    }

    @Override
    public void setMaxVisitedNodesMultiplier(float var0) {
        this.maxVisitedNodesMultiplier = var0;
    }

    @Override
    public void setRequiredPathLength(float length) {
        this.requiredPathLength = length;
        this.updatePathfinderMaxVisitedNodes();
    }

    @Override
    public void setSpeedModifier(double var0) {
        this.speedModifier = var0;
    }

    @Override
    public boolean shouldRecomputePath(final BlockPos pos) {
        if (this.hasDelayedRecomputation) {
            return false;
        } else if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0) {
            Node target = this.path.getEndNode();
            Vec3 middlePos = new Vec3((target.x + this.mob.getX()) / 2.0, (target.y + this.mob.getY()) / 2.0,
                    (target.z + this.mob.getZ()) / 2.0);
            return pos.closerToCenterThan(middlePos, this.path.getNodeCount() - this.path.getNextNodeIndex());
        } else {
            return false;
        }
    }

    private boolean shouldTargetNextNodeInDirection(final Vec3 mobPosition) {
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        } else {
            Vec3 currentNode = Vec3.atBottomCenterOf(this.path.getNextNodePos());
            if (!mobPosition.closerThan(currentNode, 2.0)) {
                return false;
            } else if (this.canMoveDirectly(mobPosition, this.path.getNextEntityPos(this.mob))) {
                return true;
            } else {
                Vec3 nextNode = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
                Vec3 mobToCurrent = currentNode.subtract(mobPosition);
                Vec3 mobToNext = nextNode.subtract(mobPosition);
                double mobToCurrentSqr = mobToCurrent.lengthSqr();
                double mobToNextSqr = mobToNext.lengthSqr();
                boolean closerToNextThanCurrent = mobToNextSqr < mobToCurrentSqr;
                boolean withinCurrentBlock = mobToCurrentSqr < 0.5;
                if (!closerToNextThanCurrent && !withinCurrentBlock) {
                    return false;
                } else {
                    Vec3 mobDirection = mobToCurrent.normalize();
                    Vec3 pathDirection = mobToNext.normalize();
                    return pathDirection.dot(mobDirection) < 0.0;
                }
            }
        }
    }

    @Override
    public void stop() {
        this.path = null;
    }

    public Path supercreatePath(BlockPos var0, int var1) {
        return createPath(ImmutableSet.of(var0), 8, false, var1);
    }

    protected void supertrimPath() {
        if (this.path != null) {
            for (int i = 0; i < this.path.getNodeCount(); ++i) {
                Node node = this.path.getNode(i);
                Node nextNode = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
                BlockState state = this.level.getBlockState(new BlockPos(node.x, node.y, node.z));
                if (state.is(BlockTags.CAULDRONS)) {
                    this.path.replaceNode(i, node.cloneAndMove(node.x, node.y + 1, node.z));
                    if (nextNode != null && node.y >= nextNode.y) {
                        this.path.replaceNode(i + 1, node.cloneAndMove(nextNode.x, node.y + 1, nextNode.z));
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        ++this.tick;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }
        if (!this.isDone()) {
            Vec3 target;
            if (this.canUpdatePath()) {
                this.followThePath();
            } else if (this.path != null && !this.path.isDone()) {
                target = this.getTempMobPos();
                Vec3 pos = this.path.getNextEntityPos(this.mob);
                if (target.y > pos.y && !this.mob.onGround() && Mth.floor(target.x) == Mth.floor(pos.x)
                        && Mth.floor(target.z) == Mth.floor(pos.z)) {
                    this.path.advance();
                }
            }
            if (!this.isDone()) {
                target = this.path.getNextEntityPos(this.mob);
                this.mvmt.getMoveControl().setWantedPosition(target.x, this.getGroundY(target), target.z,
                        this.speedModifier);
            }
        }
    }

    private void timeoutPath() {
        resetStuckTimeout();
        stop();
    }

    @Override
    protected void trimPath() {
        supertrimPath();
        if (this.avoidSun) {
            if (this.level.canSeeSky(BlockPos.containing(this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ()))) {
                return;
            }
            for (int i = 0; i < this.path.getNodeCount(); ++i) {
                Node node = this.path.getNode(i);
                if (this.level.canSeeSky(new BlockPos(node.x, node.y, node.z))) {
                    this.path.truncateNodes(i);
                    return;
                }
            }
        }
    }

    @Override
    public void updatePathfinderMaxVisitedNodes() {
        int maxVisitedNodes = Mth.floor(this.getMaxPathLength() * 16.0F);
        this.pathFinder.setMaxVisitedNodes(maxVisitedNodes);
    }
}
