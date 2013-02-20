package net.citizensnpcs.api;

import java.io.File;

import net.citizensnpcs.api.ai.speech.SpeechFactory;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.NPCSelector;
import net.citizensnpcs.api.trait.TraitFactory;

import org.bukkit.plugin.Plugin;

public interface CitizensPlugin extends Plugin {
    /**
     * @param The
     *            data store of the registry
     * @return A new anonymous NPCRegistry that is not accessible via
     *         {@link #getNamedNPCRegistry(String)}
     */
    public NPCRegistry createAnonymousNPCRegistry(NPCDataStore store);

    /**
     * @param pluginName
     *            The plugin name
     * @param store
     *            The data store for the registry
     * @return A new NPCRegistry, that can also be retrieved via
     *         {@link #getNamedNPCRegistry(String)}
     */
    public NPCRegistry createNamedNPCRegistry(String name, NPCDataStore store);

    public NPCSelector getDefaultNPCSelector();

    /**
     * 
     * @param pluginName
     *            The plugin name
     * @return A NPCRegistry previously created via
     *         {@link #createNamedNPCRegistry(String)}, or null if not found
     */
    public NPCRegistry getNamedNPCRegistry(String name);

    /**
     * Gets the <em>default</em> {@link NPCRegistry}.
     * 
     * @return The NPC registry
     */
    public NPCRegistry getNPCRegistry();

    public ClassLoader getOwningClassLoader();

    /**
     * @return The folder for storing scripts
     */
    public File getScriptFolder();

    /**
     * Gets the SpeechFactory.
     * 
     * @return Citizens speech factory
     */
    public SpeechFactory getSpeechFactory();

    /**
     * Gets the TraitFactory.
     * 
     * @return Citizens trait factory
     */
    public TraitFactory getTraitFactory();

    /**
     * Called when the current Citizens implementation is changed
     */
    public void onImplementationChanged();

    /**
     * Removes the named NPCRegistry with the given name.
     */
    public void removeNamedNPCRegistry(String name);
}
