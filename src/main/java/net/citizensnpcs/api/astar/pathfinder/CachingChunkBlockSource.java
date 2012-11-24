package net.citizensnpcs.api.astar.pathfinder;

import org.bukkit.World;

public abstract class CachingChunkBlockSource<T> extends AbstractBlockSource {
    private final Object[][] chunks;
    private final int chunkX;
    private final int chunkZ;
    protected final World world;

    protected CachingChunkBlockSource(World world, int x, int z, float radius) {
        this(world, (int) (x - radius), (int) (z - radius), (int) (x + radius), (int) (z + radius));
    }

    protected CachingChunkBlockSource(World world, int minX, int minZ, int maxX, int maxZ) {
        this.world = world;
        this.chunkX = minX >> 4;
        this.chunkZ = minZ >> 4;
        int maxChunkX = maxX >> 4, maxChunkZ = maxZ >> 4;

        chunks = new Object[maxChunkX - chunkX + 1][maxChunkZ - chunkZ + 1];
        for (int x = chunkX; x < maxChunkX; x++) {
            for (int z = chunkZ; z < maxChunkZ; z++) {
                chunks[x - chunkX][z - chunkZ] = getChunkObject(x, z);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getBlockTypeIdAt(int x, int y, int z) {
        int dX = x >> 4 - chunkX;
        int dZ = z >> 4 - chunkZ;
        if (dX >= 0 && dX < chunks.length) {
            Object[] inner = chunks[dX];
            if (dZ >= 0 && dZ < inner.length) {
                Object chunk = inner[dZ];
                if (chunk != null)
                    return getId((T) chunk, x & 15, y, z & 15);
            }
        }
        return world.getBlockTypeIdAt(x, y, z);
    }

    protected abstract T getChunkObject(int x, int z);

    protected abstract int getId(T chunk, int x, int y, int z);
}