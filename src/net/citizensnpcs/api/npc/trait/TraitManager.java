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
	 * Checks whether the given trait has been registered
	 * 
	 * @param trait
	 *            Trait to check
	 * @return Whether the given trait has been registered
	 */
	public boolean isTraitRegistered(Trait trait);

	/**
	 * Checks whether the trait with the given name has been registered
	 * 
	 * @param name
	 *            Name of the trait to check
	 * @return Whether the trait with the given name has been registered
	 */
	public boolean isTraitRegistered(String name);

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