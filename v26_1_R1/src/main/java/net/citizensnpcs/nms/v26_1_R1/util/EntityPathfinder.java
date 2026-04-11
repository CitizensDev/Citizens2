package net.citizensnpcs.nms.v26_1_R1.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import net.citizensnpcs.Settings.Setting;
import net.minecraft.core.BlockPos;
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

    public EntityPathfinder(EntityNodeEvaluator var0, int var1) {
        super(var0, var1);
        this.openSet = new BinaryHeap();
        this.nodeEvaluator = var0;
        this.maxVisitedNodes = var1;
    }

    private Path findPath(Node from, List<Map.Entry<Target, BlockPos>> targets, float maxPathLength, int reachRange,
            float maxVisitedNodesMultiplier) {
        from.g = 0.0F;
        from.h = this.getBestH(from, targets);
        from.f = from.h;
        this.openSet.clear();
        this.openSet.insert(from);
        int count = 0;
        List<Map.Entry<Target, BlockPos>> reachedTargets = Lists.newArrayListWithExpectedSize(targets.size());
        int maxVisitedNodesAdjusted = (int) (this.maxVisitedNodes * maxVisitedNodesMultiplier);

        while (!this.openSet.isEmpty()) {
            ++count;
            if (count >= maxVisitedNodesAdjusted) {
                break;
            }
            Node current = this.openSet.pop();
            current.closed = true;
            int neighborCount = 0;

            int i;
            for (i = targets.size(); neighborCount < i; ++neighborCount) {
                Map.Entry<Target, BlockPos> entry = targets.get(neighborCount);
                Target target = entry.getKey();
                if (current.distanceManhattan(target) <= reachRange) {
                    target.setReached();
                    reachedTargets.add(entry);
                }
            }
            if (!reachedTargets.isEmpty()) {
                break;
            }
            if (!(current.distanceTo(from) >= maxPathLength)) {
                neighborCount = this.nodeEvaluator.getNeighbors(this.neighbors, current);

                for (i = 0; i < neighborCount; ++i) {
                    Node neighbor = this.neighbors[i];
                    float distance = this.distance(current, neighbor);
                    neighbor.walkedDistance = current.walkedDistance + distance;
                    float tentativeGScore = current.g + distance + neighbor.costMalus;
                    if (neighbor.walkedDistance < maxPathLength
                            && (!neighbor.inOpenSet() || tentativeGScore < neighbor.g)) {
                        neighbor.cameFrom = current;
                        neighbor.g = tentativeGScore;
                        neighbor.h = this.getBestH(neighbor, targets) * 1.5F;
                        if (neighbor.inOpenSet()) {
                            this.openSet.changeCost(neighbor, neighbor.g + neighbor.h);
                        } else {
                            neighbor.f = neighbor.g + neighbor.h;
                            this.openSet.insert(neighbor);
                        }
                    }
                }
            }
        }
        Path best = null;
        boolean entryListIsEmpty = reachedTargets.isEmpty();
        Comparator<Path> comparator = entryListIsEmpty ? Comparator.comparingInt(Path::getNodeCount)
                : Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getNodeCount);
        Iterator<Map.Entry<Target, BlockPos>> var22 = (!entryListIsEmpty ? targets
                : Setting.DISABLE_MC_NAVIGATION_FALLBACK.asBoolean() ? List.<Map.Entry<Target, BlockPos>> of()
                        : reachedTargets).iterator();
        while (var22.hasNext()) {
            Map.Entry<Target, BlockPos> entry = var22.next();
            Path path = this.reconstructPath(entry.getKey().getBestNode(), entry.getValue(), !entryListIsEmpty);
            if (best == null || comparator.compare(path, best) < 0) {
                best = path;
            }
        }
        return best;
    }

    public Path findPath(PathNavigationRegion level, LivingEntity entity, Set<BlockPos> targets, float maxPathLength,
            int reachRange, float maxVisitedNodesMultiplier) {
        this.openSet.clear();
        this.nodeEvaluator.prepare(level, entity);
        Node from = this.nodeEvaluator.getStart();
        if (from == null) {
            return null;
        } else {
            List<Map.Entry<Target, BlockPos>> tos = Lists.newArrayList();
            Iterator<BlockPos> var9 = targets.iterator();

            while (var9.hasNext()) {
                BlockPos pos = var9.next();
                tos.add(new AbstractMap.SimpleEntry<>(this.nodeEvaluator.getTarget(pos.getX(), pos.getY(), pos.getZ()),
                        pos));
            }
            Path path = this.findPath(from, tos, maxPathLength, reachRange, maxVisitedNodesMultiplier);
            this.nodeEvaluator.done();
            return path;
        }
    }

    @Override
    public Path findPath(PathNavigationRegion level, Mob entity, Set<BlockPos> targets, float maxPathLength,
            int reachRange, float maxVisitedNodesMultiplier) {
        this.openSet.clear();
        this.nodeEvaluator.prepare(level, entity);
        Node from = this.nodeEvaluator.getStart();
        if (from == null) {
            return null;
        } else {
            List<Map.Entry<Target, BlockPos>> tos = Lists.newArrayList();
            Iterator<BlockPos> var9 = targets.iterator();

            while (var9.hasNext()) {
                BlockPos pos = var9.next();
                tos.add(new AbstractMap.SimpleEntry<>(this.nodeEvaluator.getTarget(pos.getX(), pos.getY(), pos.getZ()),
                        pos));
            }
            Path path = this.findPath(from, tos, maxPathLength, reachRange, maxVisitedNodesMultiplier);
            this.nodeEvaluator.done();
            return path;
        }
    }

    private float getBestH(Node from, List<Map.Entry<Target, BlockPos>> targets) {
        float bestH = Float.MAX_VALUE;
        int i = 0;

        for (int targetsSize = targets.size(); i < targetsSize; ++i) {
            Target target = targets.get(i).getKey();
            float h = from.distanceTo(target);
            target.updateBest(h, from);
            bestH = Math.min(h, bestH);
        }
        return bestH;
    }

    private Path reconstructPath(Node var0, BlockPos var1, boolean var2) {
        List<Node> var3 = new ArrayList<>();
        Node var4 = var0;
        var3.add(0, var0);

        while (var4.cameFrom != null) {
            var4 = var4.cameFrom;
            var3.add(0, var4);
        }
        return new Path(var3, var1, var2);
    }
}
