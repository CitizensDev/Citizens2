package net.citizensnpcs.npc.ai;

import org.bukkit.Chunk;
import org.bukkit.Location;

import net.citizensnpcs.api.astar.pathfinder.ChunkBlockSource;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.util.NMS;

public class NMSChunkBlockSource extends ChunkBlockSource {
    public NMSChunkBlockSource(Location location, float radius) {
        super(location, radius);
    }

    @Override
    protected BoundingBox getCollisionBox(Chunk chunk, int x, int y, int z) {
        return NMS.getCollisionBox(chunk.getBlock(x, y, z));
    }
}
