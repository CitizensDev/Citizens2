package net.citizensnpcs.api.ai;

import java.util.Objects;
import java.util.function.Supplier;

import net.citizensnpcs.api.ai.GoalController.GoalEntry;
import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.ForwardingBehaviorGoalAdapter;

public class SimpleGoalEntry implements GoalEntry {
    private final Goal goal;
    private final Supplier<Integer> priority;

    public SimpleGoalEntry(Goal goal, int priority) {
        this(goal, () -> priority);
    }

    public SimpleGoalEntry(Goal goal, Supplier<Integer> priority) {
        this.goal = goal;
        this.priority = priority;
    }

    @Override
    public int compareTo(GoalEntry o) {
        return Integer.compare(getPriority(), o.getPriority());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        SimpleGoalEntry other = (SimpleGoalEntry) obj;
        if (!Objects.equals(goal, other.goal))
            return false;
        return priority == other.priority;
    }

    @Override
    public Behavior getBehavior() {
        return goal instanceof Behavior ? (Behavior) goal
                : goal instanceof ForwardingBehaviorGoalAdapter ? ((ForwardingBehaviorGoalAdapter) goal).getWrapped()
                        : null;
    }

    @Override
    public Goal getGoal() {
        return goal;
    }

    @Override
    public int getPriority() {
        return priority.get();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * (prime + (goal == null ? 0 : goal.hashCode())) + priority.get();
    }
}