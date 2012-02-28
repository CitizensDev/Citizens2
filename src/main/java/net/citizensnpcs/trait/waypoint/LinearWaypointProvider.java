package net.citizensnpcs.trait.waypoint;

import java.util.List;

import net.citizensnpcs.api.ai.AI;
import net.citizensnpcs.api.ai.NavigationCallback;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.StorageUtils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.google.common.collect.Lists;

public class LinearWaypointProvider implements WaypointProvider {
    private final List<Waypoint> waypoints = Lists.newArrayList();

    @Override
    public Editor createEditor(final Player player) {
        return new Editor() {
            @Override
            public void begin() {
                player.sendMessage(ChatColor.GREEN + "Entered the linear waypoint editor!");
                player.sendMessage(ChatColor.GREEN + "Left click to add waypoint, right click to remove.");
            }

            @Override
            public void end() {
                player.sendMessage(ChatColor.GREEN + "Exited linear waypoint editor.");
            }

            @EventHandler
            @SuppressWarnings("unused")
            public void onPlayerInteract(PlayerInteractEvent event) {
                if (!event.getPlayer().equals(player))
                    return;
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    waypoints.add(new Waypoint(event.getClickedBlock().getLocation()));
                    player.sendMessage(ChatColor.GREEN + "Added a waypoint.");
                } else if (waypoints.size() > 0) {
                    waypoints.remove(waypoints.size() - 1);
                    player.sendMessage(ChatColor.GREEN
                            + String.format("Removed a waypoint ({0} remaining)", waypoints.size()));
                }
            }
        };
    }

    @Override
    public void load(DataKey key) {
        for (DataKey root : key.getRelative("waypoints").getIntegerSubKeys()) {
            waypoints.add(new Waypoint(StorageUtils.loadLocation(root)));
        }
    }

    @Override
    public void save(DataKey key) {
        key = key.getRelative("waypoints");
        for (int i = 0; i < waypoints.size(); ++i) {
            StorageUtils.saveLocation(key.getRelative(Integer.toString(i)), waypoints.get(i).getLocation());
        }
    }

    @Override
    public NavigationCallback getCallback() {
        return callback;
    }

    private final NavigationCallback callback = new NavigationCallback() {
        private boolean executing;
        private int currentIndex;
        private AI attached;

        @Override
        public boolean onCancel(AI ai, PathCancelReason reason) {
            if (executing) {
                executing = false;
            } else {
                executing = true;
                if (currentIndex == -1 && waypoints.size() > 0)
                    currentIndex = 0;
                if (currentIndex != -1) {
                    ai.setDestination(waypoints.get(currentIndex).getLocation());
                }
            }
            return false;
        }

        @Override
        public void onAttach(AI ai) {
            if (attached != null && attached != ai) {
                executing = false;
                currentIndex = -1;
                cycle();
                if (currentIndex != -1) {
                    ai.setDestination(waypoints.get(currentIndex).getLocation());
                }
            }
        }

        @Override
        public boolean onCompletion(AI ai) {
            if (executing) {
                cycle(); // if we're executing, we need to get the next index
            } else {
                executing = true; // we're free to return to our waypoints!
                if (currentIndex == -1 && waypoints.size() > 0)
                    currentIndex = 0;
            }
            if (currentIndex != -1) {
                ai.setDestination(waypoints.get(currentIndex).getLocation());
            }
            return false;
        }

        // TODO: problem with only 1 waypoint. Waypoint instantly completes,
        // possibly causes lag....

        private void cycle() {
            if (waypoints.size() == 0) {
                currentIndex = -1;
                return;
            }
            currentIndex++;
            if (currentIndex >= waypoints.size()) {
                currentIndex = 0;
            }
        }
    };
}
