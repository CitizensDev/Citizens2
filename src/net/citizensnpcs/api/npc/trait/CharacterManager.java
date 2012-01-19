package net.citizensnpcs.api.npc.trait;

import net.citizensnpcs.api.npc.trait.Character;

public interface CharacterManager {

	/**
	 * Registers a character to Citizens
	 * 
	 * @param character
	 *            Character to register
	 */
	public void registerCharacter(Class<? extends Character> character);

	/**
	 * Gets a character from the given name
	 * 
	 * @param name
	 *            Name of the character to get
	 * @return Character with the given name
	 */
	public Character getCharacter(String name);
}