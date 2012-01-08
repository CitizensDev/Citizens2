package net.citizensnpcs.api.npc.character;

import org.bukkit.configuration.ConfigurationSection;

public interface Trait {

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