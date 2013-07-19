package net.citizensnpcs.api.astar;

public interface Plan {
    boolean isComplete();

    void update(Agent agent);
}
