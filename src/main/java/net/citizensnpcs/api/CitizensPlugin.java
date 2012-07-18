package net.citizensnpcs.api;

import java.io.File;

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

    public File getScriptFolder();

    /**
     * Gets the TraitFactory.
     * 
     * @return Citizens trait factory
     */
    public TraitFactory getTraitFactory();

    public void onImplementationChanged();
}
