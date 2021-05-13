package net.citizensnpcs.api.astar.pathfinder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import net.citizensnpcs.api.util.SpigotUtil;

public class Path implements Plan {
    private List<Block> blockList;
    private int index = 0;
    private final PathEntry[] path;

    public Path(Collection<Vector> vector) {
        this.path = Iterables.toArray(Iterables.transform(vector, new Function<Vector, PathEntry>() {
            @Override
            public PathEntry apply(Vector input) {
                return new PathEntry(input, Collections.<PathCallback> emptyList());
            }
        }), PathEntry.class);
    }

    Path(Iterable<VectorNode> unfiltered) {
        this.path = cull(unfiltered);
    }

    private PathEntry[] cull(Iterable<VectorNode> unfiltered) {
        // possibly expose cullability in an API
        List<PathEntry> path = Lists.newArrayList();
        for (VectorNode node : unfiltered) {
            for (Vector vector : node.getPathVectors()) {
                path.add(new PathEntry(vector, node.callbacks));
            }
        }
        return path.toArray(new PathEntry[path.size()]);
    }

    public void debug() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (PathEntry entry : path) {
                if (SpigotUtil.isUsing1_13API()) {
                    player.sendBlockChange(entry.vector.toLocation(player.getWorld()), YELLOW_FLOWER.createBlockData());
                } else {
                    player.sendBlockChange(entry.vector.toLocation(player.getWorld()), YELLOW_FLOWER, (byte) 0);
                }
            }
        }
    }

    public void debugEnd() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (PathEntry entry : path) {
                Block block = entry.vector.toLocation(player.getWorld()).getBlock();
                if (SpigotUtil.isUsing1_13API()) {
                    player.sendBlockChange(block.getLocation(), block.getBlockData());
                } else {
                    player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
                }
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

        public void run(final NPC npc) {
            if (callbacks == null)
                return;
            Block current = npc.getEntity().getWorld().getBlockAt(vector.getBlockX(), vector.getBlockY(),
                    vector.getBlockZ());
            for (PathCallback callback : callbacks) {
                if (blockList == null) {
                    blockList = Lists.transform(Arrays.asList(path), new Function<PathEntry, Block>() {
                        @Override
                        public Block apply(PathEntry input) {
                            return npc.getEntity().getWorld().getBlockAt(input.vector.getBlockX(),
                                    input.vector.getBlockY(), input.vector.getBlockZ());
                        }
                    });
                }
                ListIterator<Block> vec = blockList.listIterator();
                if (index > 0) {
                    while (index != vec.nextIndex()) {
                        vec.next();
                    }
                }
                callback.run(npc, current, vec);
            }
        }

        @Override
        public String toString() {
            return vector.toString();
        }
    }

    private static Material YELLOW_FLOWER = SpigotUtil.isUsing1_13API() ? Material.SUNFLOWER
            : Material.valueOf("YELLOW_FLOWER");
}