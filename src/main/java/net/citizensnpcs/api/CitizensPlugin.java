package net.citizensnpcs.api;

import java.io.File;

import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.character.CharacterManager;
import net.citizensnpcs.api.trait.TraitManager;

import org.bukkit.plugin.Plugin;

public interface CitizensPlugin extends Plugin {
    public File getScriptFolder();

    /**
     * Gets the CharacterManager.
     * 
     * @return Citizens character manager
     */
    public CharacterManager getCharacterManager();

    /**
     * Gets the {@link NPCRegistry}.
     * 
     * @return The NPC registry
     */
    public NPCRegistry getNPCRegistry();

    /**
     * Gets the TraitManager.
     * 
     * @return Citizens trait manager
     */
    public TraitManager getTraitManager();

    public void onImplementationChanged();
}
