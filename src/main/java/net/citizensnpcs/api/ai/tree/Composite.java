package net.citizensnpcs.api.ai.tree;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

public abstract class Composite extends BehaviorGoalAdapter {
    private final List<Behavior> behaviors;

    public Composite(Behavior... behaviors) {
        this(Arrays.asList(behaviors));
    }

    public Composite(Collection<Behavior> behaviors) {
        this.behaviors = Lists.newArrayList(behaviors);
    }

    public void addBehavior(Behavior behavior) {
        behaviors.add(behavior);
    }

    protected List<Behavior> getBehaviors() {
        return behaviors;
    }

    public void removeBehavior(Behavior behavior) {
        behaviors.remove(behavior);
    }

    @Override
    public boolean shouldExecute() {
        return behaviors.size() > 0;
    }
}
