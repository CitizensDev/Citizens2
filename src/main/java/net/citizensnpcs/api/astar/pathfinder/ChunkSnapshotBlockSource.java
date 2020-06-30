package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import net.citizensnpcs.api.util.BoundingBox;

public class ChunkSnapshotBlockSource extends CachingChunkBlockSource<ChunkSnapshot> {
    public ChunkSnapshotBlockSource(Location location, float radius) {
        super(location, radius);
    }

    public ChunkSnapshotBlockSource(World world, int x, int z, float radius) {
        super(world, x, z, radius);
    }

    @Override
    protected ChunkSnapshot getChunkObject(int x, int z) {
        return world.getChunkAt(x, z).getChunkSnapshot(false, false, false);
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
