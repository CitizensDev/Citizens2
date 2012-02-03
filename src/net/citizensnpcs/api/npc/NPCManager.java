package net.citizensnpcs.api.npc;

import java.util.Collection;

import net.citizensnpcs.api.npc.trait.Character;

import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;

/**
 * Handles various NPC-related methods
 */
public interface NPCManager extends Iterable<NPC> {

    /**
     * Creates an NPC with no attached character (this does not spawn the NPC)
     * 
     * @param type
     *            Creature type to assign to the NPC
     * @param name
     *            Name to give the NPC
     * @return Created NPC
     */
    public NPC createNPC(CreatureType type, String name);

    /**
     * Creates an NPC with the given character (this does not spawn the NPC)
     * 
     * @param type
     *            Creature type to assign to the NPC
     * @param name
     *            Name to give the NPC
     * @param character
     *            Character to attach to an NPC
     * @return Created NPC with the given character
     */
    public NPC createNPC(CreatureType type, String name, Character character);

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
}