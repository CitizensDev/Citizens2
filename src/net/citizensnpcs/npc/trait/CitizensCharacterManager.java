package net.citizensnpcs.npc.trait;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.CharacterManager;

public class CitizensCharacterManager implements CharacterManager {
	private final Map<String, Class<? extends Character>> registered = new HashMap<String, Class<? extends Character>>();
	private final Set<Character> characters = new HashSet<Character>();

	@Override
	public Character getCharacter(String name) {
		if (registered.get(name) == null) {
			return null;
		}
		for (Character character : characters) {
			if (character.getName().equals(name)) {
				return character;
			}
		}
		return null;
	}

	@Override
	public void registerCharacter(Class<? extends Character> character) {
		if (registered.containsValue(character)) {
			return;
		}
		try {
			Character register = character.newInstance();
			registered.put(register.getName(), character);
			characters.add(register);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}