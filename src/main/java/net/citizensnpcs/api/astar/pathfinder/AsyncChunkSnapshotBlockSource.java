package net.citizensnpcs.api.astar.pathfinder;

import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import net.citizensnpcs.api.util.BoundingBox;

public class AsyncChunkSnapshotBlockSource extends CachingChunkBlockSource<ChunkSnapshot> {
    public AsyncChunkSnapshotBlockSource(Location location, float radius) {
        super(location, radius);
    }

    public AsyncChunkSnapshotBlockSource(World world, int x, int z, float radius) {
        super(world, x, z, radius);
    }

    @Override
    protected ChunkSnapshot getChunkObject(int x, int z) {
        // TODO: pre-load multiple chunks on cache miss
        Callable<ChunkSnapshot> call = () -> world.getChunkAt(x, z).getChunkSnapshot(false, false, false);
        try {
            if (!Bukkit.isPrimaryThread()) {
                return Bukkit.getScheduler().callSyncMethod(null, call).get();
            } else {
                return call.call();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected BoundingBox getCollisionBox(ChunkSnapshot chunk, int x, int y, int z) {
        return null; // TODO
    }

    @Override
    protected int getLightLevel(ChunkSnapshot chunk, int x, int y, int z) {
        return Math.min(15, chunk.getBlockSkyLight(x, y, z) + chunk.getBlockEmittedLight(x, y, z));
    }

    @Override
    protected Material getType(ChunkSnapshot chunk, int x, int y, int z) {
        return chunk.getBlockType(x, y, z);
    }
}
