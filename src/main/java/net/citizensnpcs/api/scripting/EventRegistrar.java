package net.citizensnpcs.api.scripting;

import java.util.Map;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.google.common.collect.Maps;

/**
 * A simple {@link ContextProvider} for scripts, allowing them to register and
 * unregister events.
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
        private final Map<FunctionReference, Listener> anonymousListeners = Maps.newHashMap();
        private final Plugin plugin;
        private final Script script;

        public Events(Plugin plugin, Script script) {
            this.plugin = plugin;
            this.script = script;
        }

        public void deregister(Object instance, String functionName) {
            if (instance == null) {
                deregister(functionName);
                return;
            }
            Listener listener = script.convertToInterface(functionName, Listener.class);
            if (listener == null)
                listener = anonymousListeners.remove(new FunctionReference(functionName, instance));
            deregisterListener(listener);
        }

        public void deregister(String functionName) {
            deregisterListener(anonymousListeners.remove(new FunctionReference(functionName, null)));
        }

        private void deregisterListener(Listener listener) {
            if (listener != null)
                HandlerList.unregisterAll(listener);
        }

        public void register(Object instance, String functionName, Class<? extends Event> eventClass) {
            registerEvent(instance, functionName, eventClass);
        }

        public void register(String functionName, Class<? extends Event> eventClass) {
            registerEvent(null, functionName, eventClass);
        }

        private void registerEvent(final Object object, final String functionName,
                final Class<? extends Event> eventClass) {
            if (!plugin.isEnabled())
                throw new IllegalStateException("Plugin is no longer valid.");
            if (functionName == null || eventClass == null)
                throw new IllegalArgumentException("Arguments should not be null");
            Listener listener = object != null ? script.convertToInterface(object, Listener.class) : null;
            if (listener == null) {
                anonymousListeners.put(new FunctionReference(functionName, object), (listener = new Listener() {
                }));
            }
            PluginManager manager = plugin.getServer().getPluginManager();
            manager.registerEvent(eventClass, listener, EventPriority.NORMAL, new EventExecutor() {
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
    }

    private static class FunctionReference {
        private final String functionName;
        private final Object instance;

        public FunctionReference(String name, Object instance) {
            this.functionName = name;
            this.instance = instance;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            FunctionReference other = (FunctionReference) obj;
            if (functionName == null) {
                if (other.functionName != null) {
                    return false;
                }
            } else if (!functionName.equals(other.functionName)) {
                return false;
            }
            if (instance == null) {
                if (other.instance != null) {
                    return false;
                }
            } else if (!instance.equals(other.instance)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = prime + ((functionName == null) ? 0 : functionName.hashCode());
            return prime * result + ((instance == null) ? 0 : instance.hashCode());
        }
    }
}
