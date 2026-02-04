package net.citizensnpcs.util;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import net.citizensnpcs.api.CitizensAPI;

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
        if (!Objects.equals(worldUUID, other.worldUUID))
            return false;

        return x == other.x && z == other.z;
    }

    public Chunk getChunk() {
        World world = getWorld();
        return world != null ? world.getChunkAt(x, z) : null;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldUUID);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * (31 + (worldUUID == null ? 0 : worldUUID.hashCode())) + x) + z;
    }

    public void setForceLoaded(boolean b) {
        if (!SUPPORTS_FORCE_LOADED)
            return;
        Chunk chunk = getChunk();
        if (chunk != null) {
            CitizensAPI.getScheduler().runTask(() -> chunk.setForceLoaded(b));
        }
    }

    @Override
    public String toString() {
        return "[" + x + "," + z + "]";
    }

    private static boolean SUPPORTS_FORCE_LOADED = true;
    static {
        try {
            Chunk.class.getMethod("setForceLoaded", boolean.class);
        } catch (NoSuchMethodException | SecurityException e) {
            SUPPORTS_FORCE_LOADED = false;
        }
    }
}