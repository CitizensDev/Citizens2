package net.citizensnpcs.api;

import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.trait.Character;
import net.citizensnpcs.api.trait.InstanceFactory;
import net.citizensnpcs.api.trait.Trait;

/**
 * Contains methods used in order to utilize the Citizens API
 */
public class CitizensAPI {
    private static final CitizensAPI instance = new CitizensAPI();

    private NPCManager npcManager;
    private InstanceFactory<Character> characterManager;
    private InstanceFactory<Trait> traitManager;

    /**
     * Gets the CharacterManager
     * 
     * @return CharacterManager
     */
    public static InstanceFactory<Character> getCharacterManager() {
        return instance.characterManager;
    }

    /**
     * Gets the NPCManager
     * 
     * @return NPCManager
     */
    public static NPCManager getNPCManager() {
        return instance.npcManager;
    }

    /**
     * Gets the TraitManager
     * 
     * @return TraitManager
     */
    public static InstanceFactory<Trait> getTraitManager() {
        return instance.traitManager;
    }

    public static void setCharacterManager(InstanceFactory<Character> characterManager) {
        if (instance.characterManager == null)
            instance.characterManager = characterManager;
    }

    public static void setNPCManager(NPCManager npcManager) {
        if (instance.npcManager == null)
            instance.npcManager = npcManager;
    }

    public static void setTraitManager(InstanceFactory<Trait> traitManager) {
        if (instance.traitManager == null)
            instance.traitManager = traitManager;
    }
}