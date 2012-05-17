package net.citizensnpcs.api;

import java.io.File;

import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.character.CharacterManager;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.trait.TraitManager;

import org.bukkit.plugin.Plugin;

/**
 * Contains methods used in order to utilize the Citizens API.
 */
public final class CitizensAPI {
    private final ScriptCompiler scriptCompiler;
    private CitizensPlugin implementation;
    {
        scriptCompiler = new ScriptCompiler();
        new Thread(scriptCompiler).start();
    }

    private CitizensAPI() {
    }

    private static final CitizensAPI instance = new CitizensAPI();

    public static File getScriptFolder() {
        return instance.implementation.getScriptFolder();
    }

    /**
     * Gets the CharacterManager.
     * 
     * @return Citizens character manager
     */
    public static CharacterManager getCharacterManager() {
        return instance.implementation.getCharacterManager();
    }

    public static Plugin getPlugin() {
        return instance.implementation;
    }

    public static File getDataFolder() {
        return instance.implementation.getDataFolder();
    }

    /**
     * Gets the {@link NPCRegistry}.
     * 
     * @return The NPC registry
     */
    public static NPCRegistry getNPCRegistry() {
        return instance.implementation.getNPCRegistry();
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
        return instance.implementation.getTraitManager();
    }

    public static void setImplementation(CitizensPlugin implementation) {
        if (instance.implementation != null)
            instance.implementation.onImplementationChanged();
        instance.implementation = implementation;
    }
}