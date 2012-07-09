package net.citizensnpcs.api.npc.character;

public interface CharacterManager {

    /**
     * Gets a character with the given name.
     * 
     * @param name
     *            Name of the character
     * @return Character with the given name (null if no Character has the given
     *         name)
     */
    public Character getCharacter(String name);

    /**
     * Registers a character using the given factory.
     * 
     * @param factory
     *            Factory to use to register a character with
     */
    public void registerCharacter(CharacterFactory factory);
}