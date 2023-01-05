package net.citizensnpcs.api;

import java.io.File;

import org.bukkit.plugin.Plugin;

import net.citizensnpcs.api.ai.speech.SpeechFactory;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.NPCSelector;
import net.citizensnpcs.api.trait.TraitFactory;

public interface CitizensPlugin extends Plugin {
    /**
     * @param store
     *            The data store of the registry
     * @return A new anonymous NPCRegistry that is not accessible via {@link #getNamedNPCRegistry(String)}
     */
    public NPCRegistry createAnonymousNPCRegistry(NPCDataStore store);

    /**
     * Creates a new <em>anonymous</em> {@link NPCRegistry} that is "Citizens-backed" i.e. will reload and unload at the
     * same time that Citizens reloads and unloads.
     *
     * @param store
     *            The {@link NPCDataStore} to use with the registry
     */
    public NPCRegistry createCitizensBackedNPCRegistry(NPCDataStore store);

    /**
     * @param name
     *            The plugin name
     * @param store
     *            The data store for the registry
     * @return A new NPCRegistry, that can also be retrieved via {@link #getNamedNPCRegistry(String)}
     */
    public NPCRegistry createNamedNPCRegistry(String name, NPCDataStore store);

    public CommandManager getCommandManager();

    /**
     * @return The default {@link NPCSelector} for managing player/server NPC selection
     */
    public NPCSelector getDefaultNPCSelector();

    /**
     * Gets the Citizens {@link LocationLookup}
     *
     * @return
     */
    public LocationLookup getLocationLookup();

    /**
     *
     * @param name
     *            The plugin name
     * @return A NPCRegistry previously created via {@link #createNamedNPCRegistry(String, NPCDataStore)}, or null if
     *         not found
     */
    public NPCRegistry getNamedNPCRegistry(String name);

    public NMSHelper getNMSHelper();

    /**
     * Get all registered {@link NPCRegistry}s.
     */
    public Iterable<NPCRegistry> getNPCRegistries();

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

    /**
     * Sets the default NPC data store. Should be set during onEnable.
     *
     * @param store
     *            The new default store
     */
    public void setDefaultNPCDataStore(NPCDataStore store);
}
