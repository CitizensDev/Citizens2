package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

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
        return BoundingBox.convert(world.getBlockAt(x, y, z).getBoundingBox());
    }

    @Override
    protected int getLightLevel(Chunk chunk, int x, int y, int z) {
        return chunk.getBlock(x, y, z).getLightLevel();
    }

    @Override
    protected Material getType(Chunk chunk, int x, int y, int z) {
        return SUPPORT_GET_TYPE ? world.getType(x << 4, y, z << 4) : chunk.getBlock(x, y, z).getType();
    }

    private static boolean SUPPORT_BOUNDING_BOX = true;
    private static boolean SUPPORT_GET_TYPE = true;
    static {
        try {
            Class.forName("org.bukkit.RegionAccessor").getMethod("getType", int.class, int.class, int.class);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            SUPPORT_GET_TYPE = false;
        }
        try {
            Block.class.getMethod("getBoundingBox");
        } catch (NoSuchMethodException | SecurityException e) {
            SUPPORT_BOUNDING_BOX = false;
        }
    }
}
