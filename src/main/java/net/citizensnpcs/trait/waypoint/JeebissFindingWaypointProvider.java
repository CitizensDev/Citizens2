package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;
import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.GoalSelector;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.ai.event.NavigatorCallback;
import net.citizensnpcs.api.astar.AStarGoal;
import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.astar.AStarNode;
import net.citizensnpcs.api.astar.Agent;
import net.citizensnpcs.api.astar.Plan;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class JeebissFindingWaypointProvider implements WaypointProvider {
    @Persist("availablewaypoints")
    private final List<Waypoint> available = Lists.newArrayList();
    private JeebissFindingWaypointProviderGoal currentGoal;
    @Persist("helperwaypoints")
    private final List<Waypoint> helpers = Lists.newArrayList();
    private NPC npc;
    private boolean paused;

    @Override
    public WaypointEditor createEditor(final Player player, CommandContext args) {
        return new WaypointEditor() {
            WaypointMarkers markers = new WaypointMarkers(player.getWorld());

            @Override
            public void begin() {
                showPath();
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_BEGIN);
            }

            private void createWaypointMarkerWithData(Waypoint element) {
                Entity entity = markers.createWaypointMarker(element);
                if (entity == null)
                    return;
                entity.setMetadata("citizens.waypointhashcode",
                        new FixedMetadataValue(CitizensAPI.getPlugin(), element.hashCode()));
            }

            @Override
            public void end() {
                Messaging.sendTr(player, Messages.LINEAR_WAYPOINT_EDITOR_END);
            }

            @EventHandler(ignoreCancelled = true)
            public void onPlayerInteract(PlayerInteractEvent event) {
                if (!event.getPlayer().equals(player) || event.getAction() == Action.PHYSICAL
                        || event.getClickedBlock() == null)
                    return;
                if (event.getPlayer().getWorld() != npc.getBukkitEntity().getWorld())
                    return;
                event.setCancelled(true);
                Location at = event.getClickedBlock().getLocation();
                Waypoint element = new Waypoint(at);
                if (player.isSneaking()) {
                    available.add(element);
                } else {
                    helpers.add(element);
                }
                createWaypointMarkerWithData(element);
            }

            @EventHandler(ignoreCancelled = true)
            public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
                if (!event.getRightClicked().hasMetadata("citizens.waypointhashcode"))
                    return;
                int hashcode = event.getRightClicked().getMetadata("citizens.waypointhashcode").get(0).asInt();
                Iterator<Waypoint> itr = Iterables.concat(available, helpers).iterator();
                while (itr.hasNext()) {
                    if (itr.next().hashCode() == hashcode) {
                        itr.remove();
                        break;
                    }
                }
            }

            private void showPath() {
                for (Waypoint element : Iterables.concat(available, helpers)) {
                    createWaypointMarkerWithData(element);
                }
            }
        };
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void load(DataKey key) {
    }

    @Override
    public void onSpawn(NPC npc) {
        this.npc = npc;
        if (currentGoal == null) {
            currentGoal = new JeebissFindingWaypointProviderGoal();
            CitizensAPI.registerEvents(currentGoal);
            npc.getDefaultGoalController().addGoal(currentGoal, 1);
        }
    }

    @Override
    public void save(DataKey key) {
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    private class JeebissFindingWaypointProviderGoal implements Goal {
        JeebissPlan plan;

        @Override
        public void reset() {
            plan = null;
        }

        @Override
        public void run(GoalSelector selector) {
            if (plan.isComplete()) {
                selector.finish();
                return;
            }
            Waypoint current = plan.getCurrentWaypoint();
            npc.getNavigator().setTarget(current.getLocation());
            npc.getNavigator().getLocalParameters().addSingleUseCallback(new NavigatorCallback() {
                @Override
                public void onCompletion(CancelReason cancelReason) {
                    plan.update(npc);
                }
            });
        }

        @Override
        public boolean shouldExecute(GoalSelector selector) {
            if (paused || available.size() == 0 || !npc.isSpawned() || npc.getNavigator().isNavigating())
                return false;
            Waypoint target = available.get(Util.getFastRandom().nextInt(available.size()));
            ASTAR.runFully(new JeebissGoal(target), null);
            return true;
        }
    }

    private static class JeebissGoal implements AStarGoal<JeebissNode> {
        private final Waypoint dest;

        public JeebissGoal(Waypoint dest) {
            this.dest = dest;
        }

        @Override
        public float g(JeebissNode from, JeebissNode to) {
            return (float) from.distance(to.waypoint);
        }

        @Override
        public float getInitialCost(JeebissNode node) {
            return h(node);
        }

        @Override
        public float h(JeebissNode from) {
            return (float) from.distance(dest);
        }

        @Override
        public boolean isFinished(JeebissNode node) {
            return node.waypoint.equals(dest);
        }
    }

    private static class JeebissNode extends AStarNode {
        private Waypoint waypoint;

        @Override
        public Plan buildPlan() {
            return new JeebissPlan(this.<JeebissNode> getParents());
        }

        public double distance(Waypoint dest) {
            return waypoint.distance(dest);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            JeebissNode other = (JeebissNode) obj;
            if (waypoint == null) {
                if (other.waypoint != null) {
                    return false;
                }
            } else if (!waypoint.equals(other.waypoint)) {
                return false;
            }
            return true;
        }

        @Override
        public Iterable<AStarNode> getNeighbours() {
            return null;
        }

        @Override
        public int hashCode() {
            return 31 + ((waypoint == null) ? 0 : waypoint.hashCode());
        }
    }

    private static class JeebissPlan implements Plan {
        private int index = 0;
        private final Waypoint[] path;

        public JeebissPlan(Iterable<JeebissNode> path) {
            this.path = Iterables.toArray(Iterables.transform(path, new Function<JeebissNode, Waypoint>() {
                @Override
                public Waypoint apply(JeebissNode to) {
                    return to.waypoint;
                }
            }), Waypoint.class);
        }

        public Waypoint getCurrentWaypoint() {
            return path[index];
        }

        @Override
        public boolean isComplete() {
            return index >= path.length;
        }

        @Override
        public void update(Agent agent) {
            index++;
        }
    }

    private static final AStarMachine<JeebissNode, JeebissPlan> ASTAR = AStarMachine.createWithDefaultStorage();
}
