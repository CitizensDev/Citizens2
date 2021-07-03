package net.citizensnpcs.api.hpastar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReversableAStarNode implements Comparable<ReversableAStarNode> {
    float g;
    float h;
    ReversableAStarNode parent;

    @Override
    public int compareTo(ReversableAStarNode o) {
        return Float.compare(g + h, o.g + o.h);
    }

    public List<ReversableAStarNode> reconstructSolution() {
        List<ReversableAStarNode> parents = new ArrayList<ReversableAStarNode>();
        ReversableAStarNode start = this;
        while (start != null) {
            parents.add(start);
            start = start.parent;
        }
        Collections.reverse(parents);
        return parents;
    }
}