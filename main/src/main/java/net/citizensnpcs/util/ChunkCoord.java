package net.citizensnpcs.util;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class ChunkCoord {
    public final UUID worldUUID;
    public final int x;
    public final int z;

    public ChunkCoord(Chunk chunk) {
        this(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
    }

    public ChunkCoord(Location loc) {
        this(loc.getWorld().getUID(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    public ChunkCoord(UUID worldUUID, int x, int z) {
        this.x = x;
        this.z = z;
        this.worldUUID = worldUUID;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        ChunkCoord other = (ChunkCoord) obj;
        if (!Objects.equals(worldUUID, other.worldUUID)) {
            return false;

        }
        return x == other.x && z == other.z;
    }

    public Chunk getChunk() {
        World world = Bukkit.getWorld(worldUUID);
        return world != null ? world.getChunkAt(x, z) : null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * (prime * (prime + (worldUUID == null ? 0 : worldUUID.hashCode())) + x) + z;
    }

    public void setForceLoaded(boolean b) {
        Chunk chunk = getChunk();
        if (chunk != null && SUPPORTS_FORCE_LOADED) {
            try {
                chunk.setForceLoaded(b);
            } catch (NoSuchMethodError e) {
                SUPPORTS_FORCE_LOADED = false;
            }
        }
    }

    @Override
    public String toString() {
        return "[" + x + "," + z + "]";
    }

    private static boolean SUPPORTS_FORCE_LOADED = true;
}