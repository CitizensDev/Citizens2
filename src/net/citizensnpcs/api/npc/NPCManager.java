package net.citizensnpcs.api.npc;

import java.util.Collection;

import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.Trait;

import org.bukkit.entity.Entity;

/**
 * Handles various NPC-related methods
 */
public interface NPCManager {

    /**
     * Creates an NPC with no attached character (this does not spawn the NPC)
     * 
     * @param name
     *            Name to give the NPC
     * @return Created NPC
     */
    public NPC createNPC(String name);

    /**
     * Creates an NPC with the given character (this does not spawn the NPC)
     * 
     * @param name
     *            Name to give the NPC
     * @param character
     *            Character to attach to an NPC
     * @return Created NPC with the given character
     */
    public NPC createNPC(String name, Character character);

    /**
     * Gets an NPC with the given ID
     * 
     * @param id
     *            ID of the NPC
     * @return NPC with the given ID (may or may not be spawned)
     */
    public NPC getNPC(int id);

    /**
     * Gets an NPC from the given LivingEntity
     * 
     * @param entity
     *            Entity to get the NPC from
     * @return NPC from the given entity (must be spawned)
     */
    public NPC getNPC(Entity entity);

    /**
     * Gets all NPCs (may or may not be spawned)
     * 
     * @return All NPCs
     */
    public Iterable<NPC> getAllNPCs();

    /**
     * Gets all spawned NPCs
     * 
     * @return All spawned NPCs
     */
    public Iterable<NPC> getSpawnedNPCs();

    /**
     * Gets all NPCs with the given trait
     * 
     * @param trait
     *            Trait to search for
     * @return All NPCs with the given trait
     */
    public Collection<NPC> getNPCs(Class<? extends Trait> trait);

    /**
     * Checks whether the given Bukkit entity is an NPC
     * 
     * @param entity
     *            Entity to check
     * @return Whether the given entity is an NPC
     */
    public boolean isNPC(Entity entity);
}