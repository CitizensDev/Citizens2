package net.citizensnpcs.api;

import java.io.File;
import java.lang.ref.WeakReference;

import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.trait.TraitFactory;

import org.bukkit.plugin.Plugin;

/**
 * Contains methods used in order to utilize the Citizens API.
 */
public final class CitizensAPI {
    private WeakReference<CitizensPlugin> implementation;

    private CitizensAPI() {
    }

    private static final CitizensAPI instance = new CitizensAPI();
    private static ScriptCompiler scriptCompiler;

    public static File getDataFolder() {
        return getImplementation().getDataFolder();
    }

    private static CitizensPlugin getImplementation() {
        return instance.implementation != null ? instance.implementation.get() : null;
    }

    /**
     * Gets the current implementation's {@link NPCRegistry}.
     * 
     * @see CitizensPlugin
     * @return The NPC registry
     */
    public static NPCRegistry getNPCRegistry() {
        return getImplementation().getNPCRegistry();
    }

    public static Plugin getPlugin() {
        return getImplementation();
    }

    public static ScriptCompiler getScriptCompiler() {
        if (scriptCompiler == null) {
            scriptCompiler = new ScriptCompiler();
            scriptCompiler.start();
        }
        return scriptCompiler;
    }

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

    public static boolean hasImplementation() {
        return getImplementation() != null;
    }

    public static void setImplementation(CitizensPlugin implementation) {
        if (implementation == null) {
            instance.implementation = null;
            return;
        }
        if (hasImplementation())
            getImplementation().onImplementationChanged();
        instance.implementation = new WeakReference<CitizensPlugin>(implementation);
    }

    public static void shutdown() {
        if (scriptCompiler != null) {
            scriptCompiler.interrupt();
            scriptCompiler = null;
        }
    }
}