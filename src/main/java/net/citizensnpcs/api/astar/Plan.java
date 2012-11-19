package net.citizensnpcs.api.astar;


public interface Plan extends Comparable<Plan> {
    boolean isComplete();

    void update();
}
