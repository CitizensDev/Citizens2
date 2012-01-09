package net.citizensnpcs.api.npc.trait;

public interface TraitManager {

	/**
	 * Registers a trait
	 * 
	 * @param trait
	 *            Trait to register
	 * @return Trait that was registered
	 */
	public Trait registerTrait(Trait Trait);

	/**
	 * Gets a trait with the given name
	 * 
	 * @param name
	 *            Name of the trait to get
	 * @return Trait with the given name
	 */
	public Trait getTrait(String name);
}