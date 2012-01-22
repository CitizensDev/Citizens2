package net.citizensnpcs.npc.trait;

import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.api.Factory;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.CharacterManager;

public class CitizensCharacterManager implements CharacterManager {
    private final Map<String, Factory<? extends Character>> registered = new HashMap<String, Factory<? extends Character>>();

    @Override
    public Character getCharacter(String name) {
        if (registered.get(name) == null)
            return null;
        return registered.get(name).create();
    }

    @Override
    public void registerCharacter(String name, Class<? extends Character> clazz) {
        registerCharacterWithFactory(name, new DefaultCharacterFactory(clazz));
    }

    @Override
    public void registerCharacterWithFactory(String name, Factory<? extends Character> factory) {
        if (registered.get(name) != null)
            throw new IllegalArgumentException("A character factory for the character '" + name
                    + "' has already been registered.");
        registered.put(name, factory);
    }

    private static class DefaultCharacterFactory implements Factory<Character> {
        private final Class<? extends Character> clazz;

        private DefaultCharacterFactory(Class<? extends Character> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Character create() {
            try {
                return clazz.newInstance();
            } catch (Exception ex) {
                return null;
            }
        }
    }
}