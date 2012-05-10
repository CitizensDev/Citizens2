package net.citizensnpcs.npc;

import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.CharacterException;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import net.citizensnpcs.api.npc.character.CharacterManager;
import net.citizensnpcs.util.Metrics;
import net.citizensnpcs.util.Metrics.Graph;
import net.citizensnpcs.util.StringHelper;

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

    public void addPlotters(Graph graph) {
        for (final Character character : registered.values()) {
            graph.addPlotter(new Metrics.Plotter(StringHelper.capitalize(character.getName())) {
                @Override
                public int getValue() {
                    return CitizensAPI.getNPCManager().getNPCs(character.getClass()).size();
                }
            });
        }

    }
}