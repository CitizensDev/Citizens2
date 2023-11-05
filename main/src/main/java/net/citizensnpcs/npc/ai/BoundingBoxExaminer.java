package net.citizensnpcs.npc.ai;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.astar.pathfinder.BlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.PathPoint;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.util.NMS;

public class BoundingBoxExaminer implements BlockExaminer {
    private double height;
    private double width;

    public BoundingBoxExaminer(Entity entity) {
        if (entity != null) {
            height = NMS.getHeight(entity);
            width = NMS.getWidth(entity);
        }
    }

    @Override
    public float getCost(BlockSource source, PathPoint point) {
        return 0;
    }

    @Override
    public PassableState isPassable(BlockSource source, PathPoint point) {
        Vector pos = point.getVector();
        Block up = source.getBlockAt(pos.getBlockX(), pos.getBlockY() + 2, pos.getBlockZ());
        Material down = source.getMaterialAt(pos.getBlockX(), pos.getBlockY() - 1, pos.getBlockZ());
        if (!MinecraftBlockExaminer.canStandIn(up) && MinecraftBlockExaminer.canStandOn(down)) {
            BoundingBox above = source.getCollisionBox(pos.getBlockX(), pos.getBlockY() + 2, pos.getBlockZ());
            BoundingBox below = source.getCollisionBox(pos.getBlockX(), pos.getBlockY() - 1, pos.getBlockZ());
            if (above == null || below == null)
                return PassableState.IGNORE;
            float height = (float) (above.minY - below.maxY);
            if (height < this.height)
                return PassableState.UNPASSABLE;

        }
        return PassableState.IGNORE;
    }
}
