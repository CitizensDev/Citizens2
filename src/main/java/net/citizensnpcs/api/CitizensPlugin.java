package net.citizensnpcs.api;

import java.io.File;

import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitManager;

import org.bukkit.plugin.Plugin;

public interface CitizensPlugin extends Plugin {
    /**
     * Gets the {@link NPCRegistry}.
     * 
     * @return The NPC registry
     */
    public NPCRegistry getNPCRegistry();

    public File getScriptFolder();

    /**
     * Gets the TraitManager.
     * 
     * @return Citizens trait manager
     */
    public TraitManager getTraitManager();

    public void onImplementationChanged();
}
