package net.citizensnpcs.api.astar.pathfinder;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.astar.Agent;
import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.astar.pathfinder.PathPoint.PathCallback;
import net.citizensnpcs.api.npc.NPC;

public class Path implements Plan {
    private int index = 0;
    private final PathEntry[] path;

    Path(Iterable<VectorNode> unfiltered) {
        this.path = cull(unfiltered);
    }

    private PathEntry[] cull(Iterable<VectorNode> unfiltered) {
        // possibly expose cullability in an API
        List<PathEntry> path = Lists.newArrayList();
        for (VectorNode node : unfiltered) {
            Vector vector = node.location;
            path.add(new PathEntry(vector, node.callbacks));
        }
        return path.toArray(new PathEntry[path.size()]);
    }

    public void debug() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (PathEntry entry : path) {
                player.sendBlockChange(entry.vector.toLocation(player.getWorld()), Material.YELLOW_FLOWER, (byte) 0);
            }
        }
    }

    public void debugEnd() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (PathEntry entry : path) {
                Block block = entry.vector.toLocation(player.getWorld()).getBlock();
                player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
            }
        }
    }

    public Vector getCurrentVector() {
        return path[index].vector;
    }

    public Iterable<Vector> getPath() {
        return Iterables.transform(Arrays.asList(path), new Function<PathEntry, Vector>() {
            @Override
            public Vector apply(PathEntry input) {
                return input.vector;
            }
        });
    }

    @Override
    public boolean isComplete() {
        return index >= path.length;
    }

    public void run(NPC npc) {
        path[index].run(npc);
    }

    @Override
    public String toString() {
        return Arrays.toString(path);
    }

    @Override
    public void update(Agent agent) {
        if (isComplete()) {
            return;
        }
        ++index;
    }

    private class PathEntry {
        final List<PathCallback> callbacks;
        final Vector vector;

        private PathEntry(Vector vector, List<PathCallback> callbacks) {
            this.vector = vector;
            this.callbacks = callbacks;
        }

        private Block getBlockUsingWorld(World world) {
            return world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
        }

        public void run(final NPC npc) {
            if (callbacks != null) {
                Block block = getBlockUsingWorld(npc.getEntity().getWorld());
                for (PathCallback callback : callbacks) {
                    ListIterator<Block> vec = Lists.transform(Arrays.asList(path), new Function<PathEntry, Block>() {
                        @Override
                        public Block apply(PathEntry input) {
                            return npc.getEntity().getWorld().getBlockAt(input.vector.getBlockX(),
                                    input.vector.getBlockY(), input.vector.getBlockZ());
                        }
                    }).listIterator();
                    if (index > 0) {
                        while (index != vec.nextIndex()) {
                            vec.next();
                        }
                    }
                    callback.run(npc, block, vec);
                }
            }
        }

        @Override
        public String toString() {
            return vector.toString();
        }
    }
}