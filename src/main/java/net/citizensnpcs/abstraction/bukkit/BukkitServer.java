package net.citizensnpcs.abstraction.bukkit;

import net.citizensnpcs.api.abstraction.Server;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class BukkitServer implements Server {
    private final Plugin plugin;

    public BukkitServer(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void callEvent(Object event) {
        Bukkit.getPluginManager().callEvent((Event) event);
    }

    @Override
    public void registerEvents(Object listener) {
        Bukkit.getPluginManager().registerEvents((Listener) listener, plugin);
    }

    @Override
    public void schedule(Runnable task) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task);
    }

    @Override
    public void schedule(Runnable task, long delay) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, delay);
    }

    @Override
    public void scheduleRepeating(Runnable task, long delay) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, 0, delay);
    }

    @Override
    public void scheduleRepeating(Runnable task, long initialDelay, long repeatDelay) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, initialDelay, repeatDelay);
    }

    @Override
    public void unregisterAll(Object listener) {
        HandlerList.unregisterAll((Listener) listener);
    }
}
