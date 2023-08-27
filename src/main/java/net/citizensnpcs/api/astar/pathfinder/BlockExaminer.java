package net.citizensnpcs.api.astar.pathfinder;

public interface BlockExaminer {
    float getCost(BlockSource source, PathPoint point);

    PassableState isPassable(BlockSource source, PathPoint point);

    public enum PassableState {
        IGNORE,
        PASSABLE,
        UNPASSABLE;
    }
}