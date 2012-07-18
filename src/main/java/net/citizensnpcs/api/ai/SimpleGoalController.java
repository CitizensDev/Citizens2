package net.citizensnpcs.api.ai;

import java.util.Collections;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class SimpleGoalController implements GoalController {
    private final List<Goal> executingGoals = Lists.newArrayList();
    private int executingPriority = -1;
    private Goal executingRootGoal;
    private final List<GoalEntry> possibleGoals = Lists.newArrayList();
    private final GoalSelector selector = new SimpleGoalSelector();
    private final List<Goal> toRemove = Lists.newArrayList();

    @Override
    public void addGoal(Goal goal, int priority) {
        Preconditions.checkNotNull(goal, "goal cannot be null");
        Preconditions.checkState(priority > 0 && priority < Integer.MAX_VALUE,
                "priority must be greater than 0");
        GoalEntry entry = new GoalEntry(goal, priority);
        if (possibleGoals.contains(entry))
            return;
        possibleGoals.add(entry);
        Collections.sort(possibleGoals);
    }

    private void addGoalToExecution(Goal goal) {
        Bukkit.getPluginManager().registerEvents(goal, CitizensAPI.getPlugin());
        executingGoals.add(goal);
    }

    private void finishCurrentGoalExecution() {
        resetGoalList();
        executingPriority = -1;
        executingRootGoal = null;
    }

    @Override
    public void removeGoal(Goal goal) {
        Preconditions.checkNotNull(goal, "goal cannot be null");
        toRemove.add(goal);
    }

    private void removePendingGoals() {
        for (int i = 0; i < toRemove.size(); ++i) {
            Goal remove = toRemove.remove(i--);

            for (int j = 0; j < possibleGoals.size(); ++j) {
                Goal test = possibleGoals.get(j).goal;
                if (!test.equals(remove))
                    continue;
                possibleGoals.remove(j--);
                if (test == executingRootGoal)
                    finishCurrentGoalExecution();
            }
        }
    }

    private void resetGoalList() {
        for (int i = 0; i < executingGoals.size(); ++i) {
            Goal goal = executingGoals.remove(i--);
            goal.reset();
            HandlerList.unregisterAll(goal);
        }
    }

    @Override
    public void run() {
        if (possibleGoals.isEmpty())
            return;
        removePendingGoals();
        trySelectGoal();

        for (int i = 0; i < executingGoals.size(); ++i) {
            executingGoals.get(i).run();
        }
    }

    private void setupExecution(GoalEntry entry) {
        finishCurrentGoalExecution();
        executingPriority = entry.priority;
        executingRootGoal = entry.goal;
        addGoalToExecution(entry.goal);
    }

    private void trySelectGoal() {
        int searchPriority = Math.min(executingPriority, 1);
        for (int i = possibleGoals.size() - 1; i >= 0; --i) {
            GoalEntry entry = possibleGoals.get(i);
            if (searchPriority > entry.priority)
                return;
            if (!entry.goal.shouldExecute(selector))
                continue;
            setupExecution(entry);
            return;
        }
    }

    private static class GoalEntry implements Comparable<GoalEntry> {
        Goal goal;
        int priority;

        private GoalEntry(Goal goal, int priority) {
            this.goal = goal;
            this.priority = priority;
        }

        @Override
        public int compareTo(GoalEntry o) {
            return o.priority > priority ? 1 : o.priority < priority ? -1 : 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            GoalEntry other = (GoalEntry) obj;
            if (goal == null) {
                if (other.goal != null) {
                    return false;
                }
            } else if (!goal.equals(other.goal)) {
                return false;
            }
            return priority == other.priority;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            return prime * (prime + ((goal == null) ? 0 : goal.hashCode())) + priority;
        }
    }

    public class SimpleGoalSelector implements GoalSelector {
        @Override
        public void finish() {
            finishCurrentGoalExecution();
        }

        @Override
        public void select(Goal goal) {
            resetGoalList();
            addGoalToExecution(goal);
        }

        @Override
        public void selectAdditional(Goal... goals) {
            for (Goal goal : goals) {
                addGoalToExecution(goal);
            }
        }
    }
}
