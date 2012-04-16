package net.citizensnpcs.api.scripting;

import org.bukkit.plugin.Plugin;

public class PluginProvider implements ContextProvider {
    private final Plugin plugin;

    public PluginProvider(Plugin plugin) {
        if (plugin == null || !plugin.isEnabled())
            throw new IllegalArgumentException("invalid plugin supplied");
        this.plugin = plugin;
    }

    @Override
    public void provide(Script script) {
        if (!plugin.isEnabled())
            throw new IllegalStateException("plugin is no longer valid");
        script.setAttribute("plugin", plugin);
    }
}
