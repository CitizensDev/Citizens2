package net.citizensnpcs.api.npc;

import java.util.Collection;

import net.citizensnpcs.api.trait.Character;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Handles various NPC-related methods
 */
public interface NPCManager extends Iterable<NPC> {

    /**
     * Creates an NPC with no attached character (this does not spawn the NPC)
     * 
     * @param type
     *            Entity type to assign to the NPC
     * @param name
     *            Name to give the NPC
     * @return Created NPC
     */
    public NPC createNPC(EntityType type, String name);

    /**
     * Creates an NPC with the given character (this does not spawn the NPC)
     * 
     * @param type
     *            Entity type to assign to the NPC
     * @param name
     *            Name to give the NPC
     * @param character
     *            Character to attach to an NPC
     * @return Created NPC with the given character
     */
    public NPC createNPC(EntityType type, String name, Character character);

    /**
     * Gets an NPC from the given LivingEntity
     * 
     * @param entity
     *            Entity to get the NPC from
     * @return NPC from the given entity (must be spawned)
     */
    public NPC getNPC(Entity entity);

    /**
     * Gets an NPC with the given ID
     * 
     * @param id
     *            ID of the NPC
     * @return NPC with the given ID (may or may not be spawned)
     */
    public NPC getNPC(int id);

    /**
     * Gets all NPCs with the given character
     * 
     * @param character
     *            Character to search for
     * @return All NPCs with the given character
     */
    public Collection<NPC> getNPCs(Class<? extends Character> character);

    /**
     * Checks whether the given Bukkit entity is an NPC
     * 
     * @param entity
     *            Entity to check
     * @return Whether the given entity is an NPC
     */
    public boolean isNPC(Entity entity);

    /**
     * Checks whether the given NPC is selected by the given player
     * 
     * @param player
     *            Player to check
     * @param npc
     *            NPC to check
     * @deprecated Will be replaced by Bukkit metadata
     * @return Whether the given NPC is selected by the given player
     */
    @Deprecated
    public boolean isNPCSelectedByPlayer(Player player, NPC npc);

    /**
     * Selects an NPC
     * 
     * @param player
     *            Player to select the NPC
     * @param npc
     *            NPC to be selected
     * @deprecated Will be replaced by Bukkit metadata
     */
    @Deprecated
    public void selectNPC(Player player, NPC npc);

    /**
     * Gets the selected NPC from the given player
     * 
     * @param player
     *            Player to check
     * @deprecated Will be replaced by Bukkit metadata
     * @return NPC that is selected by the given player
     */
    @Deprecated
    public NPC getSelectedNPC(Player player);
}