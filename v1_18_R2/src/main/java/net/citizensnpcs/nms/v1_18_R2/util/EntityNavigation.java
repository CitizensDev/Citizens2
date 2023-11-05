package net.citizensnpcs.nms.v1_18_R2.util;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import net.citizensnpcs.Settings;
import net.citizensnpcs.nms.v1_18_R2.entity.EntityHumanNPC;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class EntityNavigation extends PathNavigation {
    private boolean avoidSun;
    private final AttributeInstance followRange;
    protected boolean hasDelayedRecomputation;
    private boolean isStuck;
    protected int lastStuckCheck;
    protected Vec3 lastStuckCheckPos = Vec3.ZERO;
    protected long lastTimeoutCheck;
    protected float maxDistanceToWaypoint = 0.5F;
    private float maxVisitedNodesMultiplier = 1.0F;
    protected final LivingEntity mob;
    private final MobAI mvmt;
    protected EntityNodeEvaluator nodeEvaluator;
    protected Path path;
    private final EntityPathfinder pathFinder;
    private int reachRange;
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
        this.pathFinder = new EntityPathfinder(this.nodeEvaluator, Settings.Setting.MAXIMUM_VISITED_NODES.asInt());
        this.setRange(24);
    }

    public boolean canCutCorner(BlockPathTypes pathtype) {
        return pathtype != BlockPathTypes.DANGER_FIRE && pathtype != BlockPathTypes.DANGER_CACTUS
                && pathtype != BlockPathTypes.DANGER_OTHER && pathtype != BlockPathTypes.WALKABLE_DOOR;
    }

    @Override
    public boolean canFloat() {
        return this.nodeEvaluator.canFloat();
    }

    @Override
    protected boolean canMoveDirectly(Vec3 var0, Vec3 var1) {
        return false;
        /*
        int var5 = Mth.floor(var0.x);
        int var6 = Mth.floor(var0.z);
        double var7 = var1.x - var0.x;
        double var9 = var1.z - var0.z;
        double var11 = var7 * var7 + var9 * var9;
        if (var11 < 1.0E-8D)
            return false;
        double var13 = 1.0D / Math.sqrt(var11);
        var7 *= var13;
        var9 *= var13;
        var2 += 2;
        var4 += 2;
        if (!canWalkOn(var5, Mth.floor(var0.y), var6, var2, var3, var4, var0, var7, var9))
            return false;
        var2 -= 2;
        var4 -= 2;
        double var15 = 1.0D / Math.abs(var7);
        double var17 = 1.0D / Math.abs(var9);
        double var19 = var5 - var0.x;
        double var21 = var6 - var0.z;
        if (var7 >= 0.0D)
            var19++;
        if (var9 >= 0.0D)
            var21++;
        var19 /= var7;
        var21 /= var9;
        int var23 = (var7 < 0.0D) ? -1 : 1;
        int var24 = (var9 < 0.0D) ? -1 : 1;
        int var25 = Mth.floor(var1.x);
        int var26 = Mth.floor(var1.z);
        int var27 = var25 - var5;
        int var28 = var26 - var6;
        while (var27 * var23 > 0 || var28 * var24 > 0) {
            if (var19 < var21) {
                var19 += var15;
                var5 += var23;
                var27 = var25 - var5;
            } else {
                var21 += var17;
                var6 += var24;
                var28 = var26 - var6;
            }
            if (!canWalkOn(var5, Mth.floor(var0.y), var6, var2, var3, var4, var0, var7, var9))
                return false;
        }
        return true;*/
    }

    public boolean canOpenDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    public boolean canPassDoors() {
        return this.nodeEvaluator.canPassDoors();
    }

    @Override
    protected boolean canUpdatePath() {
        return this.mob.isOnGround() || isInLiquid() || this.mob.isPassenger();
    }

    /*
    private boolean canWalkAbove(int var0, int var1, int var2, int var3, int var4, int var5, Vec3 var6, double var7,
            double var9) {
        for (BlockPos var12 : BlockPos.betweenClosed(new BlockPos(var0, var1, var2),
                new BlockPos(var0 + var3 - 1, var1 + var4 - 1, var2 + var5 - 1))) {
            double var13 = var12.getX() + 0.5D - var6.x;
            double var15 = var12.getZ() + 0.5D - var6.z;
            if (var13 * var7 + var15 * var9 < 0.0D)
                continue;
            if (!this.level.getBlockState(var12).isPathfindable(this.level, var12, PathComputationType.LAND))
                return false;
        }
        return true;
    }
    
    private boolean canWalkOn(int var0, int var1, int var2, int var3, int var4, int var5, Vec3 var6, double var7,
            double var9) {
        int var11 = var0 - var3 / 2;
        int var12 = var2 - var5 / 2;
        if (!canWalkAbove(var11, var1, var12, var3, var4, var5, var6, var7, var9))
            return false;
        for (int var13 = var11; var13 < var11 + var3; var13++) {
            for (int var14 = var12; var14 < var12 + var5; var14++) {
                double var15 = var13 + 0.5D - var6.x;
                double var17 = var14 + 0.5D - var6.z;
                if (var15 * var7 + var17 * var9 >= 0.0D) {
                    BlockPathTypes var19 = this.nodeEvaluator.getBlockPathType(this.level, var13, var1 - 1, var14,
                            this.mob, var3, var4, var5, true, true);
                    if (!hasValidPathType(var19))
                        return false;
                    var19 = this.nodeEvaluator.getBlockPathType(this.level, var13, var1, var14, this.mob, var3, var4,
                            var5, true, true);
                    float var20 = this.mob.getPathfindingMalus(var19);
                    if (var20 < 0.0F || var20 >= 8.0F)
                        return false;
                    if (var19 == BlockPathTypes.DAMAGE_FIRE || var19 == BlockPathTypes.DANGER_FIRE
                            || var19 == BlockPathTypes.DAMAGE_OTHER)
                        return false;
                }
            }
        }
        return true;
    }*/ @Override
    public Path createPath(BlockPos var0, int var1) {
        if (this.level.getBlockState(var0).isAir()) {
            BlockPos var2 = var0.below();
            while (var2.getY() > this.level.getMinBuildHeight() && this.level.getBlockState(var2).isAir()) {
                var2 = var2.below();
            }
            if (var2.getY() > this.level.getMinBuildHeight())
                return supercreatePath(var2.above(), var1);
            while (var2.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(var2).isAir()) {
                var2 = var2.above();
            }
            var0 = var2;
        }
        if (this.level.getBlockState(var0).getMaterial().isSolid()) {
            BlockPos var2 = var0.above();
            while (var2.getY() < this.level.getMaxBuildHeight()
                    && this.level.getBlockState(var2).getMaterial().isSolid()) {
                var2 = var2.above();
            }
            return supercreatePath(var2, var1);
        }
        return supercreatePath(var0, var1);
    }

    @Override
    public Path createPath(BlockPos var0, int var1, int var2) {
        return createPath(ImmutableSet.of(var0), 8, false, var1, var2);
    }

    @Override
    public Path createPath(Entity var0, int var1) {
        return createPath(var0.blockPosition(), var1);
    }

    @Override
    public Path createPath(Set<BlockPos> var0, int var1) {
        return createPath(var0, 8, false, var1);
    }

    @Override
    protected Path createPath(Set<BlockPos> var0, int var1, boolean var2, int var3) {
        return createPath(var0, var1, var2, var3, (float) this.mob.getAttributeValue(Attributes.FOLLOW_RANGE));
    }

    @Override
    protected Path createPath(Set<BlockPos> var0, int var1, boolean var2, int var3, float var4) {
        if (var0.isEmpty() || this.mob.getY() < this.level.getMinBuildHeight() || !canUpdatePath())
            return null;
        if (this.path != null && !this.path.isDone() && var0.contains(this.targetPos))
            return this.path;
        this.level.getProfiler().push("pathfind");
        BlockPos var5 = var2 ? this.mob.blockPosition().above() : this.mob.blockPosition();
        int var6 = (int) (var4 + var1);
        PathNavigationRegion var7 = new PathNavigationRegion(this.level, var5.offset(-var6, -var6, -var6),
                var5.offset(var6, var6, var6));
        Path var8 = this.pathFinder.findPath(var7, this.mob, var0, var4, var3, this.maxVisitedNodesMultiplier);
        this.level.getProfiler().pop();
        if (var8 != null && var8.getTarget() != null) {
            this.targetPos = var8.getTarget();
            this.reachRange = var3;
            resetStuckTimeout();
        }
        return var8;
    }

    @Override
    public Path createPath(Stream<BlockPos> var0, int var1) {
        return createPath(var0.collect(Collectors.<BlockPos> toSet()), 8, false, var1);
    }

    @Override
    protected PathFinder createPathFinder(int paramInt) {
        return null;
    }

    @Override
    protected void doStuckDetection(Vec3 var0) {
        if (this.tick - this.lastStuckCheck > 100) {
            if (var0.distanceToSqr(this.lastStuckCheckPos) < 2.25D) {
                this.isStuck = true;
                stop();
            } else {
                this.isStuck = false;
            }
            this.lastStuckCheck = this.tick;
            this.lastStuckCheckPos = var0;
        }
        if (this.path != null && !this.path.isDone()) {
            BlockPos blockPos = this.path.getNextNodePos();
            if (blockPos.equals(this.timeoutCachedNode)) {
                this.timeoutTimer += System.currentTimeMillis() - this.lastTimeoutCheck;
            } else {
                this.timeoutCachedNode = blockPos;
                double var2 = var0.distanceTo(Vec3.atBottomCenterOf(this.timeoutCachedNode));
                this.timeoutLimit = this.mob.getSpeed() > 0.0F ? var2 / this.mob.getSpeed() * 1000.0D : 0.0D;
            }
            if (this.timeoutLimit > 0.0D && this.timeoutTimer > this.timeoutLimit * 3.0D) {
                timeoutPath();
            }
            this.lastTimeoutCheck = System.currentTimeMillis();
        }
    }

    @Override
    protected void followThePath() {
        Vec3 var0 = getTempMobPos();
        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F
                : 0.75F - this.mob.getBbWidth() / 2.0F;
        BlockPos blockPos = this.path.getNextNodePos();
        double var2 = Math.abs(this.mob.getX() - (blockPos.getX() + 0.5D));
        double var4 = Math.abs(this.mob.getY() - blockPos.getY());
        double var6 = Math.abs(this.mob.getZ() - (blockPos.getZ() + 0.5D));
        boolean var8 = var2 < this.maxDistanceToWaypoint && var6 < this.maxDistanceToWaypoint && var4 < 1.0D;
        if (var8 || canCutCorner(this.path.getNextNode().type) && shouldTargetNextNodeInDirection(var0)) {
            this.path.advance();
        }
        doStuckDetection(var0);
    }

    @Override
    public float getMaxDistanceToWaypoint() {
        return this.maxDistanceToWaypoint;
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
        if (!this.mob.isInWater() || !canFloat())
            return Mth.floor(this.mob.getY() + 0.5D);
        int var0 = this.mob.getBlockY();
        BlockState var1 = this.level.getBlockState(new BlockPos(this.mob.getX(), var0, this.mob.getZ()));
        int var2 = 0;
        while (var1.is(Blocks.WATER)) {
            var0++;
            var1 = this.level.getBlockState(new BlockPos(this.mob.getX(), var0, this.mob.getZ()));
            if (++var2 > 16)
                return this.mob.getBlockY();
        }
        return var0;
    }

    @Override
    public BlockPos getTargetPos() {
        return this.targetPos;
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), getSurfaceY(), this.mob.getZ());
    }

    protected boolean hasValidPathType(BlockPathTypes var0) {
        if (var0 == BlockPathTypes.WATER || var0 == BlockPathTypes.LAVA || var0 == BlockPathTypes.OPEN)
            return false;
        return true;
    }

    @Override
    public boolean isDone() {
        return this.path == null || this.path.isDone();
    }

    @Override
    protected boolean isInLiquid() {
        return this.mob.isInWaterOrBubble() || this.mob.isInLava();
    }

    @Override
    public boolean isInProgress() {
        return !isDone();
    }

    @Override
    public boolean isStableDestination(BlockPos var0) {
        BlockPos var1 = var0.below();
        return this.level.getBlockState(var1).isSolidRender(this.level, var1);
    }

    @Override
    public boolean isStuck() {
        return this.isStuck;
    }

    @Override
    public boolean moveTo(double var0, double var2, double var4, double var6) {
        return moveTo(createPath(new BlockPos(var0, var2, var4), 1), var6);
    }

    @Override
    public boolean moveTo(Entity var0, double var1) {
        Path var3 = createPath(var0, 1);
        return var3 != null && moveTo(var3, var1);
    }

    @Override
    public boolean moveTo(Path var0, double var1) {
        if (var0 == null) {
            this.path = null;
            return false;
        }
        if (!var0.sameAs(this.path)) {
            this.path = var0;
        }
        if (isDone())
            return false;
        trimPath();
        if (this.path.getNodeCount() <= 0)
            return false;
        this.speedModifier = var1;
        Vec3 var3 = getTempMobPos();
        this.lastStuckCheck = this.tick;
        this.lastStuckCheckPos = var3;
        return true;
    }

    @Override
    public void recomputePath() {
        if (this.level.getGameTime() - this.timeLastRecompute > 20L) {
            if (this.targetPos != null) {
                this.path = null;
                this.path = createPath(this.targetPos, this.reachRange);
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

    public void setRange(float pathfindingRange) {
        this.followRange.setBaseValue(pathfindingRange);
    }

    @Override
    public void setSpeedModifier(double var0) {
        this.speedModifier = var0;
    }

    @Override
    public boolean shouldRecomputePath(BlockPos var0) {
        if (this.hasDelayedRecomputation
                || ((this.path == null) || this.path.isDone() || (this.path.getNodeCount() == 0)))
            return false;
        else {
            Node var1 = this.path.getEndNode();
            Vec3 var2 = new Vec3((var1.x + this.mob.getX()) / 2.0D, (var1.y + this.mob.getY()) / 2.0D,
                    (var1.z + this.mob.getZ()) / 2.0D);
            return var0.closerToCenterThan(var2, this.path.getNodeCount() - this.path.getNextNodeIndex());
        }
    }

    private boolean shouldTargetNextNodeInDirection(Vec3 var0) {
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount())
            return false;
        Vec3 var1 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
        if (!var0.closerThan(var1, 2.0D))
            return false;
        Vec3 var2 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
        Vec3 var3 = var2.subtract(var1);
        Vec3 var4 = var0.subtract(var1);
        return var3.dot(var4) > 0.0D;
    }

    @Override
    public void stop() {
        this.path = null;
    }

    public Path supercreatePath(BlockPos var0, int var1) {
        return createPath(ImmutableSet.of(var0), 8, false, var1);
    }

    protected void supertrimPath() {
        if (this.path == null)
            return;
        for (int var0 = 0; var0 < this.path.getNodeCount(); var0++) {
            Node var1 = this.path.getNode(var0);
            Node var2 = var0 + 1 < this.path.getNodeCount() ? this.path.getNode(var0 + 1) : null;
            BlockState var3 = this.level.getBlockState(new BlockPos(var1.x, var1.y, var1.z));
            if (var3.is(BlockTags.CAULDRONS)) {
                this.path.replaceNode(var0, var1.cloneAndMove(var1.x, var1.y + 1, var1.z));
                if (var2 != null && var1.y >= var2.y) {
                    this.path.replaceNode(var0 + 1, var1.cloneAndMove(var2.x, var1.y + 1, var2.z));
                }
            }
        }
    }

    @Override
    public void tick() {
        this.tick++;
        if (this.hasDelayedRecomputation) {
            recomputePath();
        }
        if (isDone())
            return;
        if (canUpdatePath()) {
            followThePath();
        } else if (this.path != null && !this.path.isDone()) {
            Vec3 vec31 = getTempMobPos();
            Vec3 vec32 = this.path.getNextEntityPos(this.mob);
            if (vec31.y > vec32.y && !this.mob.isOnGround() && Mth.floor(vec31.x) == Mth.floor(vec32.x)
                    && Mth.floor(vec31.z) == Mth.floor(vec32.z)) {
                this.path.advance();
            }
        }
        if (isDone())
            return;
        Vec3 var0 = this.path.getNextEntityPos(this.mob);
        BlockPos var1 = new BlockPos(var0);
        mvmt.getMoveControl().setWantedPosition(var0.x, this.level.getBlockState(var1.below()).isAir() ? var0.y
                : WalkNodeEvaluator.getFloorLevel(this.level, var1), var0.z, this.speedModifier);
    }

    private void timeoutPath() {
        resetStuckTimeout();
        stop();
    }

    @Override
    protected void trimPath() {
        supertrimPath();
        if (this.avoidSun) {
            if (this.level.canSeeSky(new BlockPos(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ())))
                return;
            for (int var0 = 0; var0 < this.path.getNodeCount(); var0++) {
                Node var1 = this.path.getNode(var0);
                if (this.level.canSeeSky(new BlockPos(var1.x, var1.y, var1.z))) {
                    this.path.truncateNodes(var0);
                    return;
                }
            }
        }
    }

    private static Mob getDummyInsentient(EntityHumanNPC from, Level world) {
        return new Mob(EntityType.VILLAGER, world) {
        };
    }
}
