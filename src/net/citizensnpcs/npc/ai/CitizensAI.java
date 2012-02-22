package net.citizensnpcs.npc.ai;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.api.npc.ai.AI;
import net.citizensnpcs.api.npc.ai.Goal;
import net.citizensnpcs.api.npc.ai.NavigationCallback;
import net.citizensnpcs.api.npc.ai.NavigationCallback.PathCancelReason;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import com.google.common.collect.Lists;

public class CitizensAI implements AI {
    private Runnable ai;
    private boolean paused;
    private final List<WeakReference<NavigationCallback>> callbacks = Lists.newArrayList();
    private PathStrategy executing;
    private final List<GoalEntry> executingGoals = Lists.newArrayList();
    private final List<GoalEntry> goals = Lists.newArrayList();
    private final CitizensNPC npc;

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

    @Override
    public void registerNavigationCallback(NavigationCallback callback) {
        if (!callbacks.contains(callback))
            callbacks.add(new WeakReference<NavigationCallback>(callback));
    }

    @Override
    public void setAI(Runnable ai) {
        this.ai = ai;
    }

    @Override
    public void setDestination(Location destination) {
        if (executing != null) {
            Iterator<WeakReference<NavigationCallback>> itr = callbacks.iterator();
            while (itr.hasNext()) {
                NavigationCallback next = itr.next().get();
                if (next == null || next.onCancel(this, PathCancelReason.PLUGIN)) {
                    itr.remove();
                }
            }
        }
        executing = new MoveStrategy(npc, destination);
    }

    @Override
    public void setTarget(LivingEntity target, boolean aggressive) {
        if (executing != null) {
            Iterator<WeakReference<NavigationCallback>> itr = callbacks.iterator();
            while (itr.hasNext()) {
                NavigationCallback next = itr.next().get();
                if (next == null || next.onCancel(this, PathCancelReason.PLUGIN)) {
                    itr.remove();
                }
            }
        }
        executing = new TargetStrategy(npc, target, aggressive);
        Iterator<WeakReference<NavigationCallback>> itr = callbacks.iterator();
        while (itr.hasNext()) {
            NavigationCallback next = itr.next().get();
            if (next == null || next.onBegin(this)) {
                itr.remove();
            }
        }
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void update() {
        if (paused)
            return;
        if (executing != null && executing.update()) {
            Iterator<WeakReference<NavigationCallback>> itr = callbacks.iterator();
            while (itr.hasNext()) {
                NavigationCallback next = itr.next().get();
                if (next == null || next.onCompletion(this)) {
                    itr.remove();
                }
            }
            executing = null;
        }

        if (ai != null) {
            try {
                ai.run();
            } catch (Throwable ex) {
                Messaging.log("Unexpected error while running ai " + ai);
                ex.printStackTrace();
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