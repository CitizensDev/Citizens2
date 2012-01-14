package net.citizensnpcs.api.npc;

import java.util.Collection;

import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.Trait;

import org.bukkit.entity.LivingEntity;

/**
 * Handles various NPC-related methods
 */
public interface NPCManager {

	/**
	 * Creates an NPC with no attached characters or traits (this does not spawn
	 * the NPC)
	 * 
	 * @return Created NPC
	 */
	public NPC createNPC();

	/**
	 * Creates an NPC with the given character (this does not spawn the NPC)
	 * 
	 * @param character
	 *            Character to attach to an NPC
	 * @return Created NPC with the given character
	 */
	public NPC createNPC(Character character);

	/**
	 * Creates an NPC with the given character and individual traits (this does
	 * not spawn the NPC)
	 * 
	 * @param character
	 *            Character to attach to an NPC
	 * @param traits
	 *            Traits to give the NPC
	 * @return Created NPC with the given traits
	 */
	public NPC createNPC(Character character, Trait... traits);

	/**
	 * Gets an NPC with the given ID
	 * 
	 * @param id
	 *            ID of the NPC
	 * @return NPC with the given ID
	 */
	public NPC getNPC(int id);

	/**
	 * Gets an NPC from the given LivingEntity
	 * 
	 * @param livingEntity
	 *            LivingEntity to get the NPC from
	 * @return NPC from the given LivingEntity, null if LivingEntity is not an
	 *         NPC
	 */
	public NPC getNPC(LivingEntity livingEntity);

	/**
	 * Gets all NPCs
	 * 
	 * @return All NPCs
	 */
	public Collection<NPC> getNPCs();

	/**
	 * Gets all NPCs with the given trait
	 * 
	 * @param trait
	 *            Trait to search for
	 * @return All NPCs with the given trait
	 */
	public Collection<NPC> getNPCs(Trait trait);

	/**
	 * Gets all NPCs with the given trait
	 * 
	 * @param name
	 *            Name of the trait to search for
	 * @return All NPCs with the given trait
	 */
	public Collection<NPC> getNPCs(String name);

	/**
	 * Checks whether the given Bukkit entity is an NPC
	 * 
	 * @param livingEntity
	 *            LivingEntity to check
	 * @return Whether the given LivingEntity is an NPC
	 */
	public boolean isNPC(LivingEntity livingEntity);
}