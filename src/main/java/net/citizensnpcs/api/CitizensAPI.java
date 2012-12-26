package net.citizensnpcs.api;

import java.io.File;

import net.citizensnpcs.api.ai.speech.SpeechFactory;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.trait.TraitFactory;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

/**
 * Contains methods used in order to utilize the Citizens API.
 */
public final class CitizensAPI {
    private CitizensPlugin implementation;

    private CitizensAPI() {
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
        if (instance.implementation == null)
            throw new IllegalStateException("no implementation set");
        return instance.implementation;
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
    * Gets the current implementation's {@link SpeechFactory}.
    * 
    * @see CitizensPlugin
    * @return Citizens speech factory
    */
   public static SpeechFactory getSpeechFactory() {
       return getImplementation().getSpeechFactory();
   }
    
    /**
     * @return Whether a Citizens implementation is currently present
     */
    public static boolean hasImplementation() {
        return instance.implementation != null;
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
        if (implementation != null && hasImplementation())
            getImplementation().onImplementationChanged();
        instance.implementation = implementation;
    }

    /**
     * Shuts down any resources currently being held.
     */
    public static void shutdown() {
        if (scriptCompiler == null)
            return;
        instance.implementation = null;
        scriptCompiler.interrupt();
        scriptCompiler = null;
    }
}