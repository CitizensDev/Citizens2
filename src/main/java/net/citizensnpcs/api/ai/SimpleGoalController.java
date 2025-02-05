package net.citizensnpcs.api.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;

/**
 * A simple {@link GoalController} implementation that stores goals as a {@link ArrayList}. It works with both
 * {@link Behavior}, {@link Goal} and will also consider {@link PrioritisableGoal}s if implemented.
 */
public class SimpleGoalController implements GoalController {
    private final List<Goal> executingGoals = Lists.newArrayList();
    private int executingPriority = -1;
    private Goal executingRootGoal;
    private boolean hasPrioritisableGoal;
    private boolean paused;
    private final List<GoalEntry> possibleGoals = Lists.newArrayList();
    private final GoalSelector selector = new SimpleGoalSelector();

    @Override
    public void addBehavior(Behavior behavior, int priority) {
        if (behavior instanceof Goal) {
            addGoal((Goal) behavior, priority);
            return;
        }
        addGoal(BehaviorGoalAdapter.create(behavior), priority);
    }

    @Override
    public void addGoal(Goal goal, int priority) {
        Objects.requireNonNull(goal, "goal cannot be null");
        if (priority < 0)
            throw new IllegalArgumentException("priority must be greater than 0");
        SimpleGoalEntry entry = new SimpleGoalEntry(goal, priority);
        if (possibleGoals.contains(entry))
            return;
        possibleGoals.add(entry);
        Collections.sort(possibleGoals);
    }

    private void addGoalToExecution(Goal goal) {
        executingGoals.add(goal);
        goal.run(selector);
    }

    @Override
    public void addPrioritisableGoal(final PrioritisableGoal goal) {
        Objects.requireNonNull(goal, "goal cannot be null");
        possibleGoals.add(new SimpleGoalEntry(goal, () -> goal.getPriority()));
        hasPrioritisableGoal = true;
    }

    @Override
    public void cancelCurrentExecution() {
        finishCurrentGoalExecution();
    }

    @Override
    public void clear() {
        finishCurrentGoalExecution();
        possibleGoals.clear();
    }

    private void finishCurrentGoalExecution() {
        if (executingRootGoal == null)
            return;
        resetGoalList();
        executingPriority = -1;
        executingRootGoal = null;
    }

    @Override
    public boolean isExecutingGoal() {
        return executingRootGoal != null;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public Iterator<GoalEntry> iterator() {
        final Iterator<GoalEntry> itr = possibleGoals.iterator();
        return new Iterator<GoalEntry>() {
            GoalEntry cur;

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public GoalEntry next() {
                return cur = itr.next();
            }

            @Override
            public void remove() {
                itr.remove();
                if (cur.getGoal() == executingRootGoal) {
                    finishCurrentGoalExecution();
                }
            }
        };
    }

    @Override
    public void removeBehavior(Behavior behavior) {
        for (int i = 0; i < possibleGoals.size(); ++i) {
            Goal test = possibleGoals.get(i).getGoal();
            if (test.equals(behavior)) {
                possibleGoals.remove(i--);
                if (test == executingRootGoal) {
                    finishCurrentGoalExecution();
                }
            }
        }
    }

    @Override
    public void removeGoal(Goal goal) {
        Objects.requireNonNull(goal, "goal cannot be null");
        for (int j = 0; j < possibleGoals.size(); ++j) {
            Goal test = possibleGoals.get(j).getGoal();
            if (!test.equals(goal))
                continue;

            possibleGoals.remove(j--);
            if (test == executingRootGoal) {
                finishCurrentGoalExecution();
            }
        }
        if (goal instanceof PrioritisableGoal) {
            hasPrioritisableGoal = false;
            for (GoalEntry test : possibleGoals) {
                if (test.getGoal() instanceof PrioritisableGoal) {
                    hasPrioritisableGoal = true;
                    break;
                }
            }
        }
    }

    private void resetGoalList() {
        for (int i = 0; i < executingGoals.size(); ++i) {
            executingGoals.get(i).reset();
        }
        executingGoals.clear();
    }

    @Override
    public void run() {
        if (possibleGoals.isEmpty() || paused)
            return;
        trySelectGoal();
        for (int i = 0; i < executingGoals.size(); ++i) {
            Goal goal = executingGoals.get(i);
            goal.run(selector);
        }
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    private void setupExecution(GoalEntry entry) {
        finishCurrentGoalExecution();
        executingPriority = entry.getPriority();
        executingRootGoal = entry.getGoal();
        addGoalToExecution(entry.getGoal());
    }

    private void trySelectGoal() {
        int searchPriority = Math.max(executingPriority, 1);
        if (hasPrioritisableGoal) {
            Collections.sort(possibleGoals);
        }
        for (int hi = possibleGoals.size() - 1; hi >= 0; --hi) {
            GoalEntry entry = possibleGoals.get(hi);
            if (searchPriority > entry.getPriority())
                return;

            if (entry.getGoal() == executingRootGoal || !entry.getGoal().shouldExecute(selector))
                continue;

            if (hi == 0) {
                setupExecution(entry);
                return;
            }
            // select the goal if it has a higher priority than other goals. need to check for goals with equal priority
            // and pick one randomly if so
            for (int lo = hi - 1; lo >= 0; --lo) {
                GoalEntry next = possibleGoals.get(lo);
                if (next.getPriority() == entry.getPriority() && lo != 0)
                    continue;

                if (next.getPriority() != entry.getPriority()) {
                    lo++;
                }
                int randomSamePriorityIndex = (int) (Math.random() * ((hi + 1) - lo) + lo);
                setupExecution(possibleGoals.get(randomSamePriorityIndex));
                break;

            }
            return;
        }
    }

    public class SimpleGoalSelector implements GoalSelector {
        @Override
        public void finish() {
            finishCurrentGoalExecution();
        }

        @Override
        public void finishAndRemove() {
            Goal toRemove = executingRootGoal;
            finish();
            if (toRemove != null) {
                removeGoal(toRemove);
            }
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
