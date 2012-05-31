package net.citizensnpcs.spout;

import org.spout.api.event.Event;
import org.spout.api.event.HandlerList;
import org.spout.api.event.Listener;
import org.spout.api.scheduler.TaskPriority;

import net.citizensnpcs.api.abstraction.Server;

public class SpoutServer implements Server {
    private final CitizensSpout plugin;

    public SpoutServer(CitizensSpout plugin) {
        this.plugin = plugin;
    }

    @Override
    public void callEvent(Object event) {
        plugin.getGame().getEventManager().callEvent((Event) event);
    }

    @Override
    public void registerEvents(Object listener) {
        plugin.getGame().getEventManager().registerEvents((Listener) listener, plugin);
    }

    @Override
    public void schedule(Runnable task) {
        plugin.getGame().getScheduler().scheduleSyncDelayedTask(plugin, task);
    }

    @Override
    public void schedule(Runnable task, long delay) {
        plugin.getGame().getScheduler().scheduleSyncDelayedTask(plugin, task, delay, TaskPriority.NORMAL);
    }

    @Override
    public void scheduleRepeating(Runnable task, long delay) {
        plugin.getGame().getScheduler().scheduleSyncRepeatingTask(plugin, task, 0, delay, TaskPriority.NORMAL);
    }

    @Override
    public void scheduleRepeating(Runnable task, long initialDelay, long repeatDelay) {
        plugin.getGame().getScheduler().scheduleSyncRepeatingTask(plugin, task, initialDelay, repeatDelay, TaskPriority.NORMAL);
    }

    @Override
    public void unregisterAll(Object listener) {
        HandlerList.unregisterAll();
    }
}
