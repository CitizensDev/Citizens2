package net.citizensnpcs.api;

import java.io.File;
import java.lang.ref.WeakReference;

import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.trait.TraitManager;

import org.bukkit.plugin.Plugin;

/**
 * Contains methods used in order to utilize the Citizens API.
 */
public final class CitizensAPI {
    private WeakReference<CitizensPlugin> implementation;

    private CitizensAPI() {
    }

    private static final CitizensAPI instance = new CitizensAPI();
    private static final ScriptCompiler scriptCompiler = new ScriptCompiler();

    public static File getDataFolder() {
        return getImplementation().getDataFolder();
    }

    private static CitizensPlugin getImplementation() {
        return instance.implementation != null ? instance.implementation.get() : null;
    }

    /**
     * Gets the {@link NPCRegistry}.
     * 
     * @return The NPC registry
     */
    public static NPCRegistry getNPCRegistry() {
        return getImplementation().getNPCRegistry();
    }

    public static Plugin getPlugin() {
        return getImplementation();
    }

    public static ScriptCompiler getScriptCompiler() {
        return scriptCompiler;
    }

    public static File getScriptFolder() {
        return getImplementation().getScriptFolder();
    }

    /**
     * Gets the TraitManager.
     * 
     * @return Citizens trait manager
     */
    public static TraitManager getTraitManager() {
        return getImplementation().getTraitManager();
    }

    public static boolean hasImplementation() {
        return getImplementation() != null;
    }

    public static void setImplementation(CitizensPlugin implementation) {
        if (hasImplementation())
            getImplementation().onImplementationChanged();
        instance.implementation = new WeakReference<CitizensPlugin>(implementation);
    }

    static {
        new Thread(scriptCompiler).start();
    }
}