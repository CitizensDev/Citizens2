package net.citizensnpcs.api.ai.goals;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import ch.ethz.globis.phtree.PhTreeSolid;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.npc.NPC;

/**
 * A sample {@link Goal}/{@link Behavior} that will wander within a certain radius or {@link QuadTree}.
 */
public class WanderGoal extends BehaviorGoalAdapter implements Listener {
    private int delay;
    private int delayedTicks;
    private final Function<NPC, Location> fallback;
    private boolean forceFinish;
    private final NPC npc;
    private boolean paused;
    private final Supplier<PhTreeSolid<Boolean>> tree;
    private Object worldguardRegion;
    private int xrange;
    private int yrange;

    private WanderGoal(NPC npc, int xrange, int yrange, Supplier<PhTreeSolid<Boolean>> tree,
            Function<NPC, Location> fallback, Object worldguardRegion) {
        this.npc = npc;
        this.worldguardRegion = worldguardRegion;
        this.xrange = xrange;
        this.yrange = yrange;
        this.tree = tree;
        this.fallback = fallback;
    }

    private Location findRandomPosition() {
        Location found = MinecraftBlockExaminer.findRandomValidLocation(npc.getEntity().getLocation(NPC_LOCATION),
                xrange, yrange, new Function<Block, Boolean>() {
                    @Override
                    public Boolean apply(Block block) {
                        if ((MinecraftBlockExaminer.isLiquidOrInLiquid(block.getRelative(BlockFace.UP))
                                || MinecraftBlockExaminer.isLiquidOrInLiquid(block.getRelative(0, 2, 0)))
                                && npc.getNavigator().getDefaultParameters().avoidWater()) {
                            return false;
                        }
                        if (worldguardRegion != null) {
                            try {
                                if (!((ProtectedRegion) worldguardRegion)
                                        .contains(BukkitAdapter.asBlockVector(block.getLocation())))
                                    return false;
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                        if (tree != null) {
                            long[] pt = { block.getX(), block.getY(), block.getZ() };
                            if (tree.get() != null && !tree.get().queryIntersect(pt, pt).hasNext()) {
                                return false;
                            }
                        }
                        return true;
                    }
                }, RANDOM);
        if (found == null && fallback != null) {
            return fallback.apply(npc);
        }
        return found;
    }

    @EventHandler
    public void onFinish(NavigationCompleteEvent event) {
        forceFinish = true;
    }

    public void pause() {
        this.paused = true;
    }

    @Override
    public void reset() {
        delayedTicks = delay;
        forceFinish = false;
        HandlerList.unregisterAll(this);
    }

    @Override
    public BehaviorStatus run() {
        if (!npc.getNavigator().isNavigating() || forceFinish) {
            return BehaviorStatus.SUCCESS;
        }
        return BehaviorStatus.RUNNING;
    }

    public void setDelay(int delay) {
        this.delay = delay;
        this.delayedTicks = delay;
    }

    public void setWorldGuardRegion(Object region) {
        this.worldguardRegion = region;
    }

    public void setXYRange(int xrange, int yrange) {
        this.xrange = xrange;
        this.yrange = yrange;
    }

    @Override
    public boolean shouldExecute() {
        if (!npc.isSpawned() || npc.getNavigator().isNavigating() || paused)
            return false;
        if (delayedTicks-- > 0) {
            return false;
        }
        Location dest = findRandomPosition();
        if (dest == null)
            return false;
        npc.getNavigator().setTarget(dest);
        CitizensAPI.registerEvents(this);
        return true;
    }

    public void unpause() {
        this.paused = false;
    }

    public static class Builder {
        private Function<NPC, Location> fallback;
        private final NPC npc;
        private Supplier<PhTreeSolid<Boolean>> tree;
        private Object worldguardRegion;
        private int xrange;
        private int yrange;

        private Builder(NPC npc) {
            this.npc = npc;
            this.xrange = 10;
            this.yrange = 2;
            this.tree = null;
            this.fallback = null;
            this.worldguardRegion = null;
        }

        public WanderGoal build() {
            return new WanderGoal(npc, xrange, yrange, tree, fallback, worldguardRegion);
        }

        public Builder fallback(Function<NPC, Location> fallback) {
            this.fallback = fallback;
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

        public Builder worldguardRegion(Object worldguardRegion) {
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

    private static final Location NPC_LOCATION = new Location(null, 0, 0, 0);
    private static final Random RANDOM = new Random();
}