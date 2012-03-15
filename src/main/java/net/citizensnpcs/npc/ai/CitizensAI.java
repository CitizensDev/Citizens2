package net.citizensnpcs.npc.ai;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
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
        Iterator<WeakReference<NavigationCallback>> itr = callbacks.iterator();
        while (itr.hasNext()) {
            NavigationCallback next = itr.next().get();
            if (next == null || next.onCancel(this, CancelReason.CANCEL)) {
                itr.remove();
            }
        }
    }

    @Override
    public boolean hasDestination() {
        return executing != null;
    }

    private boolean isGoalAllowable(GoalEntry test) {
        for (GoalEntry item : goals) {
            if (item == test)
                continue;
            if (test.priority >= item.priority) {
                if (executingGoals.contains(item) && !test.goal.isCompatibleWith(item.goal)) {
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
        Iterator<WeakReference<NavigationCallback>> itr = callbacks.iterator();
        while (itr.hasNext()) {
            NavigationCallback next = itr.next().get();
            if (next == null || (replaced && next.onCancel(this, CancelReason.REPLACE)) || next.onBegin(this)) {
                itr.remove();
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
        Iterator<WeakReference<NavigationCallback>> itr = callbacks.iterator();
        while (itr.hasNext()) {
            NavigationCallback next = itr.next().get();
            if (next == null || (replaced && next.onCancel(this, CancelReason.REPLACE)) || next.onBegin(this)) {
                itr.remove();
            }
        }
    }

    public void update() {
        if (paused || !npc.isSpawned()) {
            return;
        }

        if (executing != null && executing.update()) {
            executing = null;
            Iterator<WeakReference<NavigationCallback>> itr = callbacks.iterator();
            while (itr.hasNext()) {
                NavigationCallback next = itr.next().get();
                if (next == null || next.onCompletion(this)) {
                    itr.remove();
                }
            }
        }

        for (GoalEntry entry : goals) {
            boolean executing = executingGoals.contains(entry);

            if (executing) {
                if (!entry.goal.continueExecuting() || !isGoalAllowable(entry)) {
                    entry.goal.reset();
                    executingGoals.remove(entry);
                }
            } else if (entry.goal.continueExecuting() && isGoalAllowable(entry)) {
                entry.goal.start();
                executingGoals.add(entry);
            }
        }

        for (GoalEntry entry : executingGoals) {
            entry.goal.update();
        }
    }

    private class GoalEntry implements Comparable<GoalEntry> {
        final Goal goal;
        final int priority;

        GoalEntry(int priority, Goal goal) {
            this.priority = priority;
            this.goal = goal;
        }

        @Override
        public int compareTo(GoalEntry o) {
            return o.priority > priority ? 1 : o.priority < priority ? -1 : 0;
        }
    }
}