package net.citizensnpcs.api.astar;

public interface AStarStorage {
    void close(AStarNode node);

    void open(AStarNode node);

    AStarNode removeBestNode();

    AStarNode getBestNode();

    boolean shouldExamine(AStarNode neighbour);
}
