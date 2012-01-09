package net.citizensnpcs.api.npc.trait;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Represents a Trait with a unique name that can be loaded and saved
 */
public interface Trait {

	/**
	 * Gets the unique name of this trait
	 * 
	 * @return Name of the trait
	 */
	public String getName();

	/**
	 * Loads a trait
	 * 
	 * @param configurationSection
	 *            ConfigurationSection to load from
	 */
	public void load(ConfigurationSection configurationSection);

	/**
	 * Saves a trait
	 * 
	 * @param configurationSection
	 *            ConfigurationSection to save to
	 */
	public void save(ConfigurationSection configurationSection);
}