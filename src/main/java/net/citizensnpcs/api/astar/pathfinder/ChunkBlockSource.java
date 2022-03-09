package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import net.citizensnpcs.api.util.BoundingBox;

public class ChunkBlockSource extends CachingChunkBlockSource<Chunk> {
    public ChunkBlockSource(Location location, float radius) {
        super(location, radius);
    }

    public ChunkBlockSource(World world, int x, int z, float radius) {
        super(world, x, z, radius);
    }

    @Override
    protected Chunk getChunkObject(int x, int z) {
        return world.getChunkAt(x, z);
    }

    @Override
    protected BoundingBox getCollisionBox(Chunk chunk, int x, int y, int z) {
        if (!SUPPORT_BOUNDING_BOX)
            return null;
        try {
            return BoundingBox.convert(world.getBlockAt(x, y, z).getBoundingBox());
        } catch (NoSuchMethodError e) {
            SUPPORT_BOUNDING_BOX = false;
            return null;
        }
    }

    @Override
    protected int getLightLevel(Chunk chunk, int x, int y, int z) {
        return chunk.getBlock(x, y, z).getLightLevel();
    }

    @Override
    protected Material getType(Chunk chunk, int x, int y, int z) {
        return chunk.getBlock(x, y, z).getType();
    }

    private static boolean SUPPORT_BOUNDING_BOX = true;
}
