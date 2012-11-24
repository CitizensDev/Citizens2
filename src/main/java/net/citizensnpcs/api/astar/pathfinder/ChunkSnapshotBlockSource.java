package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.ChunkSnapshot;
import org.bukkit.World;

public class ChunkSnapshotBlockSource extends CachingChunkBlockSource<ChunkSnapshot> {
    public ChunkSnapshotBlockSource(World world, int x, int z, float radius) {
        super(world, x, z, radius);
    }

    @Override
    protected ChunkSnapshot getChunkObject(int x, int z) {
        return world.getChunkAt(x, z).getChunkSnapshot(false, false, false);
    }

    @Override
    protected int getId(ChunkSnapshot chunk, int x, int y, int z) {
        return chunk.getBlockTypeId(x, y, z);
    }

    @Override
    protected int getLightLevel(ChunkSnapshot chunk, int x, int y, int z) {
        return Math.min(15, chunk.getBlockSkyLight(x, y, z) + chunk.getBlockEmittedLight(x, y, z));
    }
}
