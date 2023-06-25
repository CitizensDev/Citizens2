package net.citizensnpcs.api.ai.goals;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;

import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.tree.Behavior;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;

/**
 * A sample {@link Goal}/{@link Behavior} that simply moves an {@link NPC} through a list of {@link Location}s.
 */
public class FollowPathGoal extends BehaviorGoalAdapter {
    private int idx;
    private final NPC npc;
    private final List<MoveToGoal> path;

    public FollowPathGoal(NPC npc, List<MoveToGoal> path) {
        this.npc = npc;
        this.path = path;
    }

    @Override
    public void reset() {
        npc.getNavigator().cancelNavigation();
        idx = 0;
    }

    @Override
    public BehaviorStatus run() {
        if (idx >= path.size()) {
            return BehaviorStatus.SUCCESS;
        }
        if (!npc.getNavigator().isNavigating()) {
            setPath();
        }
        BehaviorStatus status = path.get(idx).run();
        if (status == BehaviorStatus.SUCCESS) {
            idx++;
            setPath();
        }
        return status;
    }

    private void setPath() {
        if (idx >= path.size())
            return;
        path.get(idx).shouldExecute();
    }

    @Override
    public boolean shouldExecute() {
        return !npc.getNavigator().isNavigating() && path != null;
    }

    public static FollowPathGoal create(NPC npc, List<MoveToGoal> path) {
        return new FollowPathGoal(npc, path);
    }

    public static FollowPathGoal createFromLocations(NPC npc, List<Location> path) {
        return new FollowPathGoal(npc, path.stream().map(loc -> new MoveToGoal(npc, loc)).collect(Collectors.toList()));
    }
}
