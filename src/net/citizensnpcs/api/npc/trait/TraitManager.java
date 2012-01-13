package net.citizensnpcs.api.npc.trait;

import net.citizensnpcs.api.Factory;

/**
 * Handles various trait-related methods
 */
public interface TraitManager {

	/**
	 * Gets a trait with the given name
	 * 
	 * @param name
	 *            Name of the trait to get
	 * @return Trait with the given name
	 */
	public Trait getTrait(String name);

	/**
	 * Registers a trait with the given name
	 * 
	 * @param name
	 *            Name to give the trait
	 * @param factory
	 *            Factory to produce the traits
	 */
	public void registerTrait(String name, Factory<Trait> factory);
}