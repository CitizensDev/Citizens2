package net.citizensnpcs.api.npc.character;

import net.citizensnpcs.api.exception.CharacterException;

import org.bukkit.entity.EntityType;

/**
 * Builds a character.
 */
public final class CharacterFactory {
    private final Class<? extends Character> character;
    private String name;
    private EntityType[] types;

    /**
     * Constructs a factory with the given character class.
     * 
     * @param character
     *            Class of the character
     */
    public CharacterFactory(Class<? extends Character> character) {
        this.character = character;
    }

    /**
     * Creates a character using the parameters specified using the factory's
     * methods.
     * 
     * @return Character with the factory's specified parameters
     * @throws CharacterException
     *             Thrown if the character could not be created.
     */
    public Character create() throws CharacterException {
        Character create;

        try {
            create = character.newInstance();
            // Cannot create a character without a name!
            if (name == null)
                throw new CharacterException("Character is missing a name!");
            create.setName(name);
            // Pass an empty array if the valid mob types was not set
            if (types == null)
                types = new EntityType[0];
            create.setValidTypes(types);
        } catch (Exception ex) {
            throw new CharacterException("Could not create character: " + ex.getMessage());
        }

        return create;
    }

    /**
     * Assigns a name to the character. This is used as a key to save character
     * data.
     * 
     * @param name
     *            Name to assign to the character
     */
    public CharacterFactory withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Specifies which mob types that the character can be given upon creation.
     * Default is all types.
     * 
     * @param types
     *            List of types that the character can be
     */
    public CharacterFactory withTypes(EntityType... types) {
        this.types = types;
        return this;
    }
}