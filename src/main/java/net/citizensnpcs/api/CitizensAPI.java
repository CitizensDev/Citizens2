package net.citizensnpcs.api;

import java.io.File;

import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.api.npc.character.CharacterManager;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.trait.TraitManager;

/**
 * Contains methods used in order to utilize the Citizens API.
 */
public final class CitizensAPI {
    private CharacterManager characterManager;
    private File dataFolder;
    private NPCManager npcManager;
    private final ScriptCompiler scriptCompiler;
    private TraitManager traitManager;
    {
        scriptCompiler = new ScriptCompiler();
        new Thread(scriptCompiler).start();
    }

    private CitizensAPI() {
    }

    public static File getScriptFolder() {
        return new File(instance.dataFolder, "scripts");
    }

    private static final CitizensAPI instance = new CitizensAPI();

    /**
     * Gets the CharacterManager.
     * 
     * @return Citizens character manager
     */
    public static CharacterManager getCharacterManager() {
        return instance.characterManager;
    }

    public static File getDataFolder() {
        return instance.dataFolder;
    }

    /**
     * Gets the NPCManager.
     * 
     * @return Citizens NPC manager
     */
    public static NPCManager getNPCManager() {
        return instance.npcManager;
    }

    public static ScriptCompiler getScriptCompiler() {
        return instance.scriptCompiler;
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

    public static void setDataFolder(File file) {
        if (instance.dataFolder == null)
            instance.dataFolder = file;
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