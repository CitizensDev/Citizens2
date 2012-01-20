package net.citizensnpcs.api;

import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.trait.CharacterManager;
import net.citizensnpcs.api.npc.trait.TraitManager;

/**
 * Contains methods used in order to utilize the Citizens API
 */
public class CitizensAPI {
    private static final CitizensAPI instance = new CitizensAPI();

    private NPCManager npcManager;
    private CharacterManager characterManager;
    private TraitManager traitManager;

    /**
     * Gets the NPCManager
     * 
     * @return NPCManager
     */
    public static NPCManager getNPCManager() {
        return instance.npcManager;
    }

    /**
     * Gets the CharacterManager
     * 
     * @return CharacterManager
     */
    public static CharacterManager getCharacterManager() {
        return instance.characterManager;
    }

    /**
     * Gets the TraitManager
     * 
     * @return TraitManager
     */
    public static TraitManager getTraitManager() {
        return instance.traitManager;
    }

    public static void setNPCManager(NPCManager npcManager) {
        if (instance.npcManager == null) {
            instance.npcManager = npcManager;
        }
    }

    public static void setCharacterManager(CharacterManager characterManager) {
        if (instance.characterManager == null) {
            instance.characterManager = characterManager;
        }
    }

    public static void setTraitManager(TraitManager traitManager) {
        if (instance.traitManager == null) {
            instance.traitManager = traitManager;
        }
    }
}