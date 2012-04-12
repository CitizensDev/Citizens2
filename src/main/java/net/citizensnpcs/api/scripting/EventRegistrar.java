package net.citizensnpcs.api.scripting;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * TODO: Add JavaDoc
 */
public class EventRegistrar implements ContextProvider {
    private final Plugin plugin;

    public EventRegistrar(Plugin plugin) {
        if (plugin == null || !plugin.isEnabled())
            throw new IllegalArgumentException("Invalid plugin passed to EventRegistrar. Is it enabled?");
        this.plugin = plugin;
    }

    @Override
    public void provide(Script script) {
        script.setAttribute("events", new Events(plugin, script));
    }

    public static class Events {
        private final Plugin plugin;
        private final Script script;

        public Events(Plugin plugin, Script script) {
            this.plugin = plugin;
            this.script = script;
        }

        public void register(String functionName, Class<? extends Event> eventClass) {
            registerEvent(null, functionName, eventClass);
        }

        private void registerEvent(final Object object, final String functionName,
                final Class<? extends Event> eventClass) {
            if (!plugin.isEnabled())
                throw new IllegalStateException("Plugin is no longer valid.");
            PluginManager manager = plugin.getServer().getPluginManager();
            manager.registerEvent(eventClass, new Listener() {
            }, EventPriority.NORMAL, new EventExecutor() {
                @Override
                public void execute(Listener listener, Event event) throws EventException {
                    try {
                        if (!eventClass.isAssignableFrom(event.getClass()))
                            return;
                        if (object != null) {
                            script.invoke(object, functionName, event);
                        } else {
                            script.invoke(functionName, event);
                        }
                    } catch (Throwable t) {
                        throw new EventException(t);
                    }
                }
            }, plugin);
        }

        public void register(Object instance, String functionName, Class<? extends Event> eventClass) {
            registerEvent(instance, functionName, eventClass);
        }
    }
}