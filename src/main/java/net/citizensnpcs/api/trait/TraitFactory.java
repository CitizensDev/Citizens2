package net.citizensnpcs.api.trait;

import org.bukkit.plugin.Plugin;

/**
 * Builds a trait.
 */
public final class TraitFactory {
    private Plugin plugin;
    private final Class<? extends Trait> trait;
    private String name;

    /**
     * Constructs a factory with the given trait class.
     * 
     * @param character
     *            Class of the trait
     */
    public TraitFactory(Class<? extends Trait> trait) {
        this.trait = trait;
    }

    public Class<? extends Trait> getTraitClass() {
        return trait;
    }

    public Plugin getTraitPlugin() {
        return plugin;
    }

    public String getTraitName() {
        return name;
    }

    public TraitFactory withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Attaches a plugin instance to a trait. This is used for dynamically
     * registering and unregistering listeners per trait.
     * 
     * @param plugin
     *            Plugin to attach to a trait
     * @return This TraitFactory
     */
    public TraitFactory withPlugin(Plugin plugin) {
        this.plugin = plugin;
        return this;
    }
}