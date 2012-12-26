package net.citizensnpcs.api;

import java.io.File;

import net.citizensnpcs.api.ai.speech.SpeechFactory;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitFactory;

import org.bukkit.plugin.Plugin;

public interface CitizensPlugin extends Plugin {
    /**
     * Gets the {@link NPCRegistry}.
     * 
     * @return The NPC registry
     */
    public NPCRegistry getNPCRegistry();

    /**
     * @return The folder for storing scripts
     */
    public File getScriptFolder();

    /**
     * Gets the TraitFactory.
     * 
     * @return Citizens trait factory
     */
    public TraitFactory getTraitFactory();
    
    /**
     * Gets the SpeechFactory.
     * 
     * @return Citizens speech factory
     */
    public SpeechFactory getSpeechFactory();

    /**
     * Called when the current Citizens implementation is changed
     */
    public void onImplementationChanged();
}
