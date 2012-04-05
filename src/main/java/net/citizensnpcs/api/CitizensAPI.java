package net.citizensnpcs.api;

import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.character.CharacterManager;
import net.citizensnpcs.api.trait.TraitManager;

/**
 * Contains methods used in order to utilize the Citizens API.
 */
public final class CitizensAPI {
    private static final CitizensAPI instance = new CitizensAPI();

    private CharacterManager characterManager;
    private NPCManager npcManager;
    private TraitManager traitManager;

    private CitizensAPI() {
    }

    /**
     * Gets the CharacterManager.
     * 
     * @return Citizens character manager
     */
    public static CharacterManager getCharacterManager() {
        return instance.characterManager;
    }

    /**
     * Gets the NPCManager.
     * 
     * @return Citizens NPC manager
     */
    public static NPCManager getNPCManager() {
        return instance.npcManager;
    }

    /**
     * Gets the TraitManager.
     * 
     * @return Citizens trait manager
     */
    public static TraitManager getTraitManager() {
        return instance.traitManager;
    }

    public static void setCharacterManager(CharacterManager characterManager) {
        if (instance.characterManager == null)
            instance.characterManager = characterManager;
    }

    public static void setNPCManager(NPCManager npcManager) {
        if (instance.npcManager == null)
            instance.npcManager = npcManager;
    }

    public static void setTraitManager(TraitManager traitManager) {
        if (instance.traitManager == null)
            instance.traitManager = traitManager;
    }
}