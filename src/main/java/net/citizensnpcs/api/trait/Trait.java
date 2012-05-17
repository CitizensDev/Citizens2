package net.citizensnpcs.api.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.plugin.Plugin;

/**
 * Represents a Trait that can be loaded and saved.
 */
public abstract class Trait {
    private String name;
    private Plugin plugin;

    /**
     * Gets the name of this trait.
     * 
     * @return Name of this trait
     */
    public final String getName() {
        return name;
    }

    /**
     * Gets the plugin that this trait is associated with.
     * 
     * @return Plugin attached to this trait
     */
    public final Plugin getPlugin() {
        return plugin;
    }

    /**
     * Loads a trait.
     * 
     * @param key
     *            DataKey to load from
     * @throws NPCLoadException
     *             Thrown if this trait failed to load properly
     */
    public abstract void load(DataKey key) throws NPCLoadException;

    /**
     * Called when an NPC is spawned. NPCs cannot be physically modified until
     * the entity is created in-game. This is called after the entity has been
     * created.
     */
    public void onNPCSpawn() {
    }

    /**
     * Called when a trait is removed from the given NPC.
     */
    public void onRemove() {
    }

    /**
     * Saves a trait.
     * 
     * @param key
     *            DataKey to save to
     */
    public abstract void save(DataKey key);

    public final void setName(String name) {
        if (this.name != null)
            throw new IllegalArgumentException("Cannot change the name of a trait");
        this.name = name;
    }

    public final void setPlugin(Plugin plugin) {
        if (this.plugin != null)
            throw new IllegalArgumentException("Cannot change the plugin of a trait.");

        this.plugin = plugin;
    }
}