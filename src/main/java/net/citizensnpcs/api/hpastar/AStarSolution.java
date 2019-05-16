package net.citizensnpcs.api.hpastar;

import java.util.Collection;
import java.util.List;

import org.bukkit.util.Vector;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class AStarSolution {
    final float cost;
    private final List<SimpleAStarNode> path;

    public AStarSolution(List<SimpleAStarNode> path, float cost) {
        this.path = path;
        this.cost = cost;
    }

    public Collection<Vector> convertToVectors() {
        return Lists.transform(path, new Function<SimpleAStarNode, Vector>() {
            @Override
            public Vector apply(SimpleAStarNode input) {
                HPAGraphNode node = ((HPAGraphAStarNode) input).node;
                return new Vector(node.x, node.y, node.z);
            }
        });
    }
}