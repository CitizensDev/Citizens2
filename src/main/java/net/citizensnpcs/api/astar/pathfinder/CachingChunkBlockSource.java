package net.citizensnpcs.api.astar.pathfinder;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.api.util.SpigotUtil;

public abstract class CachingChunkBlockSource<T> extends BlockSource {
    private final Map<ChunkCoord, ChunkCache> chunkCache = Maps.newHashMap();
    private final Object[][] chunks;
    private final int chunkX;
    private final int chunkZ;
    protected final World world;

    protected CachingChunkBlockSource(Location location, float radius) {
        this(location.getWorld(), location.getBlockX(), location.getBlockZ(), radius);
    }

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

    protected abstract T getChunkObject(int x, int z);

    @Override
    public BoundingBox getCollisionBox(int x, int y, int z) {
        if (!SpigotUtil.checkYSafe(y, world)) {
            return BoundingBox.EMPTY;
        }
        T chunk = getSpecific(x, z);
        if (chunk != null)
            return getCollisionBox(chunk, x & 15, y, z & 15);
        if (!SUPPORT_BOUNDING_BOX) {
            return null;
        }
        try {
            return BoundingBox.convert(world.getBlockAt(x, y, z).getBoundingBox());
        } catch (NoSuchMethodError e) {
            SUPPORT_BOUNDING_BOX = false;
            return null;
        }
    }

    protected abstract BoundingBox getCollisionBox(T chunk, int x, int y, int z);

    protected abstract int getLightLevel(T chunk, int x, int y, int z);

    @Override
    public Material getMaterialAt(int x, int y, int z) {
        if (!SpigotUtil.checkYSafe(y, world)) {
            return Material.AIR;
        }
        T chunk = getSpecific(x, z);
        if (chunk != null)
            return getType(chunk, x & 15, y, z & 15);
        return world.getBlockAt(x, y, z).getType();
    }

    @SuppressWarnings("unchecked")
    private T getSpecific(int x, int z) {
        int xx = (x >> 4) - chunkX;
        int zz = (z >> 4) - chunkZ;
        if (xx >= 0 && xx < chunks.length) {
            Object[] inner = chunks[xx];
            if (zz >= 0 && zz < inner.length) {
                return (T) inner[zz];
            }
        }
        ChunkCoord key = new ChunkCoord(x >> 4, z >> 4);
        ChunkCache prev = chunkCache.get(key);
        if (prev == null) {
            chunkCache.put(key, prev = new ChunkCache());
        } else if (prev.obj != null) {
            return prev.obj;
        } else if (++prev.hitCount >= 2) {
            return prev.obj = getChunkObject(x >> 4, z >> 4);
        }
        return null;
    }

    protected abstract Material getType(T chunk, int x, int y, int z);

    @Override
    public World getWorld() {
        return world;
    }

    private class ChunkCache {
        int hitCount;
        T obj;
    }

    private static class ChunkCoord {
        int x, z;

        public ChunkCoord(int xx, int zz) {
            this.x = xx;
            this.z = zz;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            ChunkCoord other = (ChunkCoord) obj;
            return x == other.x && z == other.z;
        }

        @Override
        public int hashCode() {
            int result = 31 * x;
            return 31 * result + z;
        }
    }

    private static boolean SUPPORT_BOUNDING_BOX = true;
}