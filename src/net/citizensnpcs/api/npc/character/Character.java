package net.citizensnpcs.api.npc.character;

import java.util.Set;

import net.citizensnpcs.api.npc.NPC;

public interface Character {

	/**
	 * Gets the unique name of this character
	 * 
	 * @return Name of the character
	 */
	public String getName();

	/**
	 * Called when an NPC is left-clicked
	 * 
	 * @param npc
	 *            NPC that was left-clicked
	 */
	public void onLeftClick(NPC<?> npc);

	/**
	 * Called when an NPC is right-clicked
	 * 
	 * @param npc
	 *            NPC that was right-clicked
	 */
	public void onRightClick(NPC<?> npc);

	/**
	 * Gets a set of traits from this character
	 * 
	 * @return Set of registered traits from this character
	 */
	public Set<Trait> getTraits();

	/**
	 * Adds a trait to this character
	 * 
	 * @param trait
	 *            Trait to add to this character
	 */
	public void addTrait(Class<? extends Trait> trait);

	/**
	 * Removes a trait from this character
	 * 
	 * @param trait
	 *            Trait to remove from this character
	 */
	public void removeTrait(Class<? extends Trait> trait);
}