package net.citizensnpcs.trait.waypoint;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.ai.goals.WanderGoal;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

public class WanderWaypointProvider implements WaypointProvider {
    private Goal currentGoal;
    private NPC npc;
    private volatile boolean paused;
    @Persist
    public int xrange = DEFAULT_XRANGE;
    @Persist
    public int yrange = DEFAULT_YRANGE;

    @Override
    public WaypointEditor createEditor(final CommandSender sender, CommandContext args) {
        return new WaypointEditor() {
            @Override
            public void begin() {
                Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_BEGIN);
            }

            @Override
            public void end() {
                Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_END);
            }

            @EventHandler(ignoreCancelled = true)
            public void onPlayerChat(AsyncPlayerChatEvent event) {
                if (!event.getPlayer().equals(sender))
                    return;
                String message = event.getMessage().toLowerCase();
                if (message.startsWith("xrange") || message.startsWith("yrange")) {
                    event.setCancelled(true);
                    int range = 0;
                    try {
                        range = Integer.parseInt(message.split(" ", 2)[1]);
                        if (range <= 0) {
                            range = 0;
                        }
                        if (message.startsWith("xrange")) {
                            xrange = range;
                        } else {
                            yrange = range;
                        }
                    } catch (Exception ex) {
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), new Runnable() {
                        @Override
                        public void run() {
                            Messaging.sendTr(sender, Messages.WANDER_WAYPOINTS_RANGE_SET, xrange, yrange);
                        }
                    });
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
    public void onRemove() {
        npc.getDefaultGoalController().removeGoal(currentGoal);
    }

    @Override
    public void onSpawn(NPC npc) {
        this.npc = npc;
        if (currentGoal == null) {
            currentGoal = WanderGoal.createWithNPCAndRange(npc, xrange, yrange);
        }
        npc.getDefaultGoalController().addGoal(currentGoal, 1);
    }

    @Override
    public void save(DataKey key) {
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    private static final int DEFAULT_XRANGE = 3;
    private static final int DEFAULT_YRANGE = 25;
}
