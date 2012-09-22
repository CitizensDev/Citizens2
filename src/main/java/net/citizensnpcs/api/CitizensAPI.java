package net.citizensnpcs.api;

import java.io.File;
import java.lang.ref.WeakReference;

import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.NPCSelector;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.trait.TraitFactory;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Contains methods used in order to utilize the Citizens API.
 */
public final class CitizensAPI {
    private WeakReference<CitizensPlugin> implementation;

    private CitizensAPI() {
    }

    /**
     * @return The {@link NPCSelector} of the current implementation
     */
    public NPCSelector getNPCSelector() {
        return getImplementation().getNPCSelector();
    }

    private static final CitizensAPI instance = new CitizensAPI();
    private static ScriptCompiler scriptCompiler;

    /**
     * @return The data folder of the current implementation
     */
    public static File getDataFolder() {
        return getImplementation().getDataFolder();
    }

    private static CitizensPlugin getImplementation() {
        return instance.implementation != null ? instance.implementation.get() : null;
    }

    /**
     * Gets the current implementation's {@link NPCRegistry}.
     * 
     * @return The NPC registry
     */
    public static NPCRegistry getNPCRegistry() {
        return getImplementation().getNPCRegistry();
    }

    /**
     * @return The current {@link Plugin} providing an implementation
     */
    public static Plugin getPlugin() {
        return getImplementation();
    }

    /**
     * @return The current {@link ScriptCompiler}
     */
    public static ScriptCompiler getScriptCompiler() {
        if (scriptCompiler == null) {
            scriptCompiler = new ScriptCompiler();
            scriptCompiler.start();
        }
        return scriptCompiler;
    }

    /**
     * @return The folder used for storing scripts
     */
    public static File getScriptFolder() {
        return getImplementation().getScriptFolder();
    }

    /**
     * Gets the current implementation's {@link TraitFactory}.
     * 
     * @see CitizensPlugin
     * @return Citizens trait factory
     */
    public static TraitFactory getTraitFactory() {
        return getImplementation().getTraitFactory();
    }

    /**
     * @return Whether a Citizens implementation is currently present
     */
    public static boolean hasImplementation() {
        return getImplementation() != null;
    }

    /**
     * A helper method for registering events using the current implementation's
     * {@link Plugin}.
     * 
     * @see #getPlugin()
     * @param listener
     *            The listener to register events for
     */
    public static void registerEvents(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, getPlugin());
    }

    /**
     * Sets the current Citizens implementation.
     * 
     * @param implementation
     *            The new implementation
     */
    public static void setImplementation(CitizensPlugin implementation) {
        if (implementation == null) {
            instance.implementation = null;
            return;
        }
        if (hasImplementation())
            getImplementation().onImplementationChanged();
        instance.implementation = new WeakReference<CitizensPlugin>(implementation);
    }

    /**
     * Shuts down any resources currently being held.
     */
    public static void shutdown() {
        if (scriptCompiler != null) {
            scriptCompiler.interrupt();
            scriptCompiler = null;
        }
    }
}