package net.citizensnpcs.npc.ai;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import net.citizensnpcs.api.ai.AI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.NavigationCallback;
import net.citizensnpcs.api.ai.NavigationCallback.CancelReason;
import net.citizensnpcs.npc.CitizensNPC;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import com.google.common.collect.Lists;

public class CitizensAI implements AI {
    private final List<WeakReference<NavigationCallback>> callbacks = Lists.newArrayList();
    private PathStrategy executing;
    private final List<GoalEntry> executingGoals = Lists.newArrayList();
    private final List<GoalEntry> goals = Lists.newArrayList();
    private List<Goal> toRemove = null;
    private final CitizensNPC npc;
    private boolean paused;

    public CitizensAI(CitizensNPC npc) {
        this.npc = npc;
    }

    @Override
    public void addGoal(int priority, Goal goal) {
        if (goals.contains(goal))
            return;
        goals.add(new GoalEntry(priority, goal));
        Collections.sort(goals);
    }

    @Override
    public void cancelDestination() {
        if (executing == null)
            return;
        executing = null;
        for (int i = 0; i < callbacks.size(); ++i) {
            NavigationCallback next = callbacks.get(i).get();
            if (next == null || next.onCancel(this, CancelReason.CANCEL)) {
                callbacks.remove(i);
            }
        }
    }

    @Override
    public boolean hasDestination() {
        return executing != null;
    }

    private boolean isGoalAllowable(GoalEntry test) {
        for (int i = 0; i < goals.size(); ++i) {
            GoalEntry item = goals.get(i);
            if (item == test)
                continue;
            if (test.priority >= item.priority) {
                if (!test.goal.isCompatibleWith(item.goal) && executingGoals.contains(item)) {
                    return false;
                }
            } /*else if (executingGoals.contains(item) && !item.goal.requiresUpdates()) {
                return false;
              }*/
        }

        return true;
    }

    public void pause() {
        paused = true;
    }

    @Override
    public void registerNavigationCallback(NavigationCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(new WeakReference<NavigationCallback>(callback));
            callback.onAttach(this);
        }
    }

    @Override
    public void removeGoal(Goal goal) {
        if (toRemove == null)
            toRemove = Lists.newArrayList();
        toRemove.add(goal);
    }

    public void resume() {
        paused = false;
    }

    @Override
    public void setDestination(Location destination) {
        if (destination == null)
            throw new IllegalArgumentException("destination cannot be null");
        boolean replaced = executing != null;
        executing = new MCNavigationStrategy(npc, destination);

        if (!replaced)
            return;

        for (int i = 0; i < callbacks.size(); ++i) {
            NavigationCallback next = callbacks.get(i).get();
            if (next == null || (replaced && next.onCancel(this, CancelReason.REPLACE)) || next.onBegin(this)) {
                callbacks.remove(i);
            }
        }
    }

    @Override
    public void setTarget(LivingEntity target, boolean aggressive) {
        if (target == null)
            throw new IllegalArgumentException("target cannot be null");

        boolean replaced = executing != null;
        executing = new MCTargetStrategy(npc, target, aggressive);

        if (!replaced)
            return;
        for (int i = 0; i < callbacks.size(); ++i) {
            NavigationCallback next = callbacks.get(i).get();
            if (next == null || (replaced && next.onCancel(this, CancelReason.REPLACE)) || next.onBegin(this)) {
                callbacks.remove(i);
            }
        }
    }

    public void update() {
        if (paused || !npc.isSpawned()) {
            return;
        }

        if (executing != null && executing.update()) {
            executing = null;
            for (int i = 0; i < callbacks.size(); ++i) {
                NavigationCallback next = callbacks.get(i).get();
                if (next == null || next.onCompletion(this)) {
                    callbacks.remove(i);
                }
            }
        }
        removeGoals();
        for (int i = 0; i < goals.size(); ++i) {
            GoalEntry entry = goals.get(i);
            boolean executing = executingGoals.contains(entry);

            if (executing) {
                if (!entry.goal.continueExecuting() || !isGoalAllowable(entry)) {
                    entry.goal.reset();
                    executingGoals.remove(entry);
                }
            } else if (entry.goal.shouldExecute() && isGoalAllowable(entry)) {
                entry.goal.start();
                executingGoals.add(entry);
            }
        }

        for (int i = 0; i < executingGoals.size(); ++i) {
            executingGoals.get(i).goal.update();
        }
    }

    private void removeGoals() {
        if (toRemove == null)
            return;
        for (Goal goal : toRemove) {
            for (int i = 0; i < executingGoals.size(); ++i) {
                GoalEntry entry = executingGoals.get(i);
                if (entry.goal.equals(goal)) {
                    entry.goal.reset();
                    executingGoals.remove(i);
                }
            }
            for (int i = 0; i < goals.size(); ++i) {
                GoalEntry entry = goals.get(i);
                if (entry.goal.equals(goal))
                    goals.remove(i);
            }
        }

        toRemove = null;
    }

    public static class GoalEntry implements Comparable<GoalEntry> {
        final Goal goal;
        final int priority;

        public GoalEntry(int priority, Goal goal) {
            this.priority = priority;
            this.goal = goal;
        }

        @Override
        public int compareTo(GoalEntry o) {
            return o.priority > priority ? 1 : o.priority < priority ? -1 : 0;
        }

        public Goal getGoal() {
            return goal;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((goal == null) ? 0 : goal.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
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
            return true;
        }
    }
}