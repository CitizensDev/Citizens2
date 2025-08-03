package net.citizensnpcs.api.ai.goals;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import ch.ethz.globis.phtree.PhTreeSolid;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.npc.NPC;

/**
 * A sample {@link Goal}/{@link Behavior} that will wander within a certain block region.
 */
public class WanderGoal extends BehaviorGoalAdapter implements Listener {
    private int delay;
    private int delayedTicks;
    private Function<Block, Boolean> filter;
    private boolean forceFinish;
    private int movingTicks;
    private final NPC npc;
    private boolean pathfind;
    private boolean paused;
    private final Function<NPC, Location> picker;
    private Location target;
    private final Supplier<PhTreeSolid<Boolean>> tree;
    private final Supplier<Object> worldguardRegion;
    private int xrange;
    private int yrange;

    private WanderGoal(NPC npc, boolean pathfind, int xrange, int yrange, Supplier<PhTreeSolid<Boolean>> tree,
            Supplier<Object> worldguardRegion, int delay, Function<Block, Boolean> filter,
            Function<NPC, Location> picker) {
        this.npc = npc;
        this.pathfind = pathfind;
        this.worldguardRegion = worldguardRegion;
        this.xrange = xrange;
        this.yrange = yrange;
        this.tree = tree;
        this.delay = delay;
        this.picker = picker;
        this.filter = filter == null ? block -> {
            if (npc.getNavigator().getDefaultParameters().avoidWater()
                    && (MinecraftBlockExaminer.isLiquidOrInLiquid(block.getRelative(BlockFace.UP))
                            || MinecraftBlockExaminer.isLiquidOrInLiquid(block.getRelative(0, 2, 0))))
                return false;
            if (worldguardRegion != null) {
                Object region = worldguardRegion.get();
                if (region != null) {
                    try {
                        if (!((ProtectedRegion) region).contains(BukkitAdapter.asBlockVector(block.getLocation())))
                            return false;
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
            if (tree != null) {
                long[] pt = { block.getX(), block.getY(), block.getZ() };
                if (tree.get() != null && !tree.get().queryIntersect(pt, pt).hasNext())
                    return false;
            }
            return true;
        } : filter;
    }

    private Location findRandomPosition() {
        if (picker != null)
            return picker.apply(npc);

        return MinecraftBlockExaminer.findRandomValidLocation(npc.getStoredLocation(), pathfind ? xrange : 1,
                pathfind ? yrange : 1, filter, RANDOM);
    }

    public void pause() {
        this.paused = true;
        if (target != null) {
            if (pathfind) {
                npc.getNavigator().cancelNavigation();
            } else {
                npc.setMoveDestination(null);
            }
        }
    }

    @Override
    public void reset() {
        target = null;
        movingTicks = 0;
        delayedTicks = delay;
        forceFinish = false;
    }

    @Override
    public BehaviorStatus run() {
        if (paused || forceFinish)
            return BehaviorStatus.SUCCESS;
        if (pathfind) {
            if (!npc.getNavigator().isNavigating())
                return BehaviorStatus.SUCCESS;
        } else {
            if (target.getWorld() != npc.getEntity().getWorld())
                return BehaviorStatus.SUCCESS;

            if (npc.getEntity().getLocation().distance(target) >= 0.1) {
                npc.setMoveDestination(target);
            } else
                return BehaviorStatus.SUCCESS;
            if (movingTicks-- <= 0) {
                npc.setMoveDestination(null);
                return BehaviorStatus.SUCCESS;
            }
        }
        return BehaviorStatus.RUNNING;
    }

    public void setDelay(int delayTicks) {
        this.delay = delayTicks;
        this.delayedTicks = delayTicks;
        forceFinish = true;
    }

    public void setPathfind(boolean pathfind) {
        this.pathfind = pathfind;
        forceFinish = true;
    }

    public void setXYRange(int xrange, int yrange) {
        this.xrange = xrange;
        this.yrange = yrange;
        forceFinish = true;
    }

    @Override
    public boolean shouldExecute() {
        if (!npc.isSpawned() || npc.getNavigator().isNavigating() || paused || delayedTicks-- > 0)
            return false;

        Location dest = findRandomPosition();
        if (dest == null)
            return false;

        if (pathfind) {
            npc.getNavigator().setTarget(dest);
            npc.getNavigator().getLocalParameters().stuckAction(null);
            npc.getNavigator().getLocalParameters().addSingleUseCallback(reason -> forceFinish = true);
        } else {
            Random random = new Random();
            dest.setX(dest.getX() + random.nextDouble() * 0.5);
            dest.setZ(dest.getZ() + random.nextDouble() * 0.5);
            movingTicks = 20 + random.nextInt(20);
        }
        this.target = dest;
        return true;
    }

    public void unpause() {
        this.paused = false;
    }

    public static class Builder {
        private int delay = 10;
        private Function<Block, Boolean> filter;
        private final NPC npc;
        private boolean pathfind = true;
        private Function<NPC, Location> picker;
        private Supplier<PhTreeSolid<Boolean>> tree;
        private Supplier<Object> worldguardRegion;
        private int xrange = 10;
        private int yrange = 2;

        private Builder(NPC npc) {
            this.npc = npc;
        }

        public WanderGoal build() {
            return new WanderGoal(npc, pathfind, xrange, yrange, tree, worldguardRegion, delay, filter, picker);
        }

        public Builder delay(int delay) {
            this.delay = delay;
            return this;
        }

        public Builder destinationPicker(Function<NPC, Location> picker) {
            this.picker = picker;
            return this;
        }

        public Builder filter(Function<Block, Boolean> filter) {
            this.filter = filter;
            return this;
        }

        public Builder pathfind(boolean pathfind) {
            this.pathfind = pathfind;
            return this;
        }

        public Builder regionCentres(Supplier<Iterable<Location>> supplier) {
            this.tree = () -> {
                PhTreeSolid<Boolean> tree = PhTreeSolid.create(3);
                for (Location loc : supplier.get()) {
                    long[] lower = { loc.getBlockX() - xrange, loc.getBlockY() - yrange, loc.getBlockZ() - xrange };
                    long[] upper = { loc.getBlockX() + xrange, loc.getBlockY() + yrange, loc.getBlockZ() + xrange };
                    tree.put(lower, upper, true);
                }
                return tree;
            };
            return this;
        }

        public Builder tree(Supplier<PhTreeSolid<Boolean>> supplier) {
            this.tree = supplier;
            return this;
        }

        public Builder worldguardRegion(Supplier<Object> worldguardRegion) {
            this.worldguardRegion = worldguardRegion;
            return this;
        }

        public Builder xrange(int xrange) {
            this.xrange = xrange;
            return this;
        }

        public Builder yrange(int yrange) {
            this.yrange = yrange;
            return this;
        }
    }

    public static Builder builder(NPC npc) {
        return new Builder(npc);
    }

    private static final Random RANDOM = new Random();
}