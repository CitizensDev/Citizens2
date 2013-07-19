package net.citizensnpcs.api.astar.pathfinder;

public interface BlockExaminer {
    float getCost(BlockSource source, PathPoint point);

    boolean isPassable(BlockSource source, PathPoint point);
}