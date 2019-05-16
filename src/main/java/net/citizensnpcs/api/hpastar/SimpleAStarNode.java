package net.citizensnpcs.api.hpastar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleAStarNode implements Comparable<SimpleAStarNode> {
    float g;
    float h;
    SimpleAStarNode parent;

    @Override
    public int compareTo(SimpleAStarNode o) {
        return Float.compare(g + h, o.g + o.h);
    }

    public List<SimpleAStarNode> reconstructSolution() {
        List<SimpleAStarNode> parents = new ArrayList<SimpleAStarNode>();
        SimpleAStarNode start = this;
        while (start != null) {
            parents.add(start);
            start = start.parent;
        }
        Collections.reverse(parents);
        return parents;
    }
}