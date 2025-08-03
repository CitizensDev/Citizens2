package net.citizensnpcs.nms.v1_18_R2.util;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.citizensnpcs.Settings.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.Target;

public class EntityPathfinder extends PathFinder {
    private final int maxVisitedNodes;
    private final Node[] neighbors = new Node[32];
    private final EntityNodeEvaluator nodeEvaluator;
    private final BinaryHeap openSet;

    public EntityPathfinder() {
        super(null, Setting.MAXIMUM_VISITED_NODES.asInt());
        this.nodeEvaluator = new EntityNodeEvaluator();
        this.openSet = new BinaryHeap();
        this.maxVisitedNodes = Setting.MAXIMUM_VISITED_NODES.asInt();
    }

    public EntityPathfinder(EntityNodeEvaluator var0, int var1) {
        super(var0, var1);
        this.openSet = new BinaryHeap();
        this.nodeEvaluator = var0;
        this.maxVisitedNodes = var1;
    }

    public Path findPath(PathNavigationRegion var0, LivingEntity var1, Set<BlockPos> var2, float var3, int var4,
            float var5) {
        this.openSet.clear();
        this.nodeEvaluator.prepare(var0, var1);
        Node var6 = this.nodeEvaluator.getStart();
        Map<Target, BlockPos> var7 = var2.stream().collect(
                Collectors.toMap(p -> this.nodeEvaluator.getGoal(p.getX(), p.getY(), p.getZ()), Function.identity()));
        Path var8 = findPath(var0.getProfiler(), var6, var7, var3, var4, var5);
        this.nodeEvaluator.done();
        return var8;
    }

    @Override
    public Path findPath(PathNavigationRegion var0, Mob var1, Set<BlockPos> var2, float var3, int var4, float var5) {
        this.openSet.clear();
        this.nodeEvaluator.prepare(var0, var1);
        Node var6 = this.nodeEvaluator.getStart();
        Map<Target, BlockPos> var7 = var2.stream().collect(
                Collectors.toMap(p -> this.nodeEvaluator.getGoal(p.getX(), p.getY(), p.getZ()), Function.identity()));
        Path var8 = findPath(var0.getProfiler(), var6, var7, var3, var4, var5);
        this.nodeEvaluator.done();
        return var8;
    }

    private Path findPath(ProfilerFiller var0, Node var1, Map<Target, BlockPos> var2, float var3, int var4,
            float var5) {
        var0.push("find_path");
        var0.markForCharting(MetricCategory.PATH_FINDING);
        Set<Target> var6 = var2.keySet();
        var1.g = 0.0F;
        var1.h = getBestH(var1, var6);
        var1.f = var1.h;
        this.openSet.clear();
        this.openSet.insert(var1);
        int var8 = 0;
        Set<Target> var9 = Sets.newHashSetWithExpectedSize(var6.size());
        int var10 = (int) (this.maxVisitedNodes * var5);
        while (!this.openSet.isEmpty() && ++var8 < var10) {
            Node node = this.openSet.pop();
            node.closed = true;
            for (Target target : var6) {
                if (node.distanceManhattan(target) <= var4) {
                    target.setReached();
                    var9.add(target);
                }
            }
            if (!var9.isEmpty()) {
                break;
            }
            if (node.distanceTo(var1) >= var3) {
                continue;
            }
            int i = this.nodeEvaluator.getNeighbors(this.neighbors, node);
            for (int var13 = 0; var13 < i; var13++) {
                Node var14 = this.neighbors[var13];
                float var15 = node.distanceTo(var14);
                node.walkedDistance += var15;
                float var16 = node.g + var15 + var14.costMalus;
                if (var14.walkedDistance < var3 && (!var14.inOpenSet() || var16 < var14.g)) {
                    var14.cameFrom = node;
                    var14.g = var16;
                    var14.h = getBestH(var14, var6) * 1.5F;
                    if (var14.inOpenSet()) {
                        this.openSet.changeCost(var14, var14.g + var14.h);
                    } else {
                        var14.f = var14.g + var14.h;
                        this.openSet.insert(var14);
                    }
                }
            }
        }
        Optional<Path> var11 = !var9.isEmpty()
                ? var9.stream().map(p -> reconstructPath(p.getBestNode(), var2.get(p), true)).min(
                        Comparator.comparingInt(Path::getNodeCount))
                : getFallbackDestinations(var2, var6).findFirst();
        var0.pop();
        if (!var11.isPresent())
            return null;
        Path var12 = var11.get();
        return var12;
    }

    private float getBestH(Node var0, Set<Target> var1) {
        float var2 = Float.MAX_VALUE;
        for (Target var4 : var1) {
            float var5 = var0.distanceTo(var4);
            var4.updateBest(var5, var0);
            var2 = Math.min(var5, var2);
        }
        return var2;
    }

    public Stream<Path> getFallbackDestinations(Map<Target, BlockPos> var1, Set<Target> var5) {
        if (Setting.DISABLE_MC_NAVIGATION_FALLBACK.asBoolean())
            return Stream.empty();
        return var5.stream().map(var1x -> this.reconstructPath(var1x.getBestNode(), var1.get(var1x), false))
                .sorted(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getNodeCount));
    }

    private Path reconstructPath(Node var0, BlockPos var1, boolean var2) {
        List<Node> var3 = Lists.newArrayList();
        Node var4 = var0;
        var3.add(0, var4);
        while (var4.cameFrom != null) {
            var4 = var4.cameFrom;
            var3.add(0, var4);
        }
        return new Path(var3, var1, var2);
    }
}
