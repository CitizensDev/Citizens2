package net.citizensnpcs.api.npc.character;

public interface CharacterManager {

	/**
	 * Registers a character
	 * 
	 * @param character
	 *            Character to register
	 * @return Character that was registered
	 */
	public Character registerCharacter(Character character);
}