package net.citizensnpcs.npc;

import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.api.exception.CharacterException;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import net.citizensnpcs.api.npc.character.CharacterManager;

public class CitizensCharacterManager implements CharacterManager {
    private final Map<String, Character> registered = new HashMap<String, Character>();

    @Override
    public Character getCharacter(String name) {
        return registered.get(name);
    }

    @Override
    public void registerCharacter(CharacterFactory factory) {
        try {
            Character character = factory.create();
            registered.put(character.getName(), character); // TODO: this only
                                                            // allows singletons
                                                            // for characters.
        } catch (CharacterException ex) {
            ex.printStackTrace();
        }
    }
    public Iterable<Character> getRegistered(){
        return registered.values();
    }
}