package net.citizensnpcs.api.ai;

import java.util.Collections;
import java.util.Iterator;
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
    private volatile boolean paused;
    private final List<SimpleGoalEntry> possibleGoals = Lists.newArrayList();
    private final GoalSelector selector = new SimpleGoalSelector();

    @Override
    public void addGoal(Goal goal, int priority) {
        Preconditions.checkNotNull(goal, "goal cannot be null");
        Preconditions.checkState(priority > 0 && priority < Integer.MAX_VALUE, "priority must be greater than 0");
        SimpleGoalEntry entry = new SimpleGoalEntry(goal, priority);
        if (possibleGoals.contains(entry))
            return;
        possibleGoals.add(entry);
        Collections.sort(possibleGoals);
    }

    private void addGoalToExecution(Goal goal) {
        if (CitizensAPI.hasImplementation())
            Bukkit.getPluginManager().registerEvents(goal, CitizensAPI.getPlugin());
        executingGoals.add(goal);
    }

    @Override
    public void clear() {
        finishCurrentGoalExecution();
        possibleGoals.clear();
    }

    private void finishCurrentGoalExecution() {
        resetGoalList();
        executingPriority = -1;
        HandlerList.unregisterAll(executingRootGoal);
        executingRootGoal = null;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public Iterator<GoalEntry> iterator() {
        final Iterator<SimpleGoalEntry> itr = possibleGoals.iterator();
        return new Iterator<GoalEntry>() {
            GoalEntry cur;

            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public GoalEntry next() {
                return (cur = itr.next());
            }

            @Override
            public void remove() {
                itr.remove();
                if (cur.getGoal() == executingRootGoal)
                    finishCurrentGoalExecution();
            }
        };
    }

    @Override
    public void removeGoal(Goal goal) {
        Preconditions.checkNotNull(goal, "goal cannot be null");
        for (int j = 0; j < possibleGoals.size(); ++j) {
            Goal test = possibleGoals.get(j).goal;
            if (!test.equals(goal))
                continue;
            possibleGoals.remove(j--);
            if (test == executingRootGoal)
                finishCurrentGoalExecution();
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
        if (possibleGoals.isEmpty() || paused)
            return;
        trySelectGoal();
        for (int i = 0; i < executingGoals.size(); ++i) {
            executingGoals.get(i).run(selector);
        }
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    private void setupExecution(SimpleGoalEntry entry) {
        finishCurrentGoalExecution();
        executingPriority = entry.priority;
        executingRootGoal = entry.goal;
        addGoalToExecution(entry.goal);
    }

    private void trySelectGoal() {
        int searchPriority = Math.max(executingPriority, 1);
        for (int i = possibleGoals.size() - 1; i >= 0; --i) {
            SimpleGoalEntry entry = possibleGoals.get(i);
            if (searchPriority > entry.priority)
                return;
            if (entry.goal == executingRootGoal || !entry.goal.shouldExecute(selector))
                continue;
            if (i == 0) {
                setupExecution(entry);
                return;
            }
            for (int j = i - 1; j >= 0; --j) {
                SimpleGoalEntry next = possibleGoals.get(j);
                boolean unequalPriorities = next.priority != entry.priority;
                if (unequalPriorities || j == 0) {
                    if (unequalPriorities)
                        j++; // we want the previous entry where entry.priority
                             // == next.priority
                    int ran = (int) Math.floor(Math.random() * (i - j + 1) + j);
                    if (ran >= possibleGoals.size() || ran < 0) {
                        setupExecution(entry);
                        break;
                    }
                    SimpleGoalEntry selected = possibleGoals.get(ran);
                    if (selected.priority != entry.priority) {
                        setupExecution(entry);
                        break;
                    }
                    setupExecution(selected);
                    break;
                }
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
            if (toRemove != null)
                removeGoal(toRemove);
        }

        @Override
        public void select(Goal goal) {
            resetGoalList();
            addGoalToExecution(goal);
        }

        @Override
        public void selectAdditional(Goal... goals) {
            for (Goal goal : goals)
                addGoalToExecution(goal);
        }
    }
}
