package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.Trait;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * Handles various NPC-related methods
 */
public interface NPCManager {

	/**
	 * Creates an NPC with no attached characters or traits
	 * 
	 * @return Created NPC
	 */
	public NPC<?> createNPC();

	/**
	 * Creates an NPC with the given NPC character
	 * 
	 * @param character
	 *            Character to attach to an NPC
	 * @return Created NPC with given NPC character
	 */
	public NPC<?> createNPC(Class<? extends Character> character);

	/**
	 * Spawns an NPC at the given location
	 * 
	 * @param npc
	 *            NPC to spawn
	 * @param location
	 *            Location to spawn the NPC
	 */
	public void spawnNPC(NPC<?> npc, Location location);

	/**
	 * Despawns an NPC
	 * 
	 * @param npc
	 *            NPC to despawn
	 */
	public void despawnNPC(NPC<?> npc);

	/**
	 * Despawns an NPC
	 * 
	 * @param id
	 *            ID of the NPC to despawn
	 */
	public void despawnNPC(int id);

	/**
	 * Gets an NPC from the given ID
	 * 
	 * @param id
	 *            ID of the NPC
	 * @return NPC with the given ID
	 */
	public NPC<?> getNPC(int id);

	/**
	 * Gets an NPC from the given LivingEntity
	 * 
	 * @param livingEntity
	 *            LivingEntity to get the NPC from
	 * @return NPC from the given LivingEntity, null if LivingEntity is not an
	 *         NPC
	 */
	public NPC<?> getNPC(LivingEntity livingEntity);

	/**
	 * Gets all NPCs
	 * 
	 * @return All NPCs
	 */
	public NPC<?>[] getNPCs();

	/**
	 * Gets all NPCs with the given trait
	 * 
	 * @param name
	 *            Name of the trait to search for
	 * @return All NPCs with the given trait
	 */
	public NPC<?>[] getNPCs(String name);

	/**
	 * Gets all NPCs with the given trait
	 * 
	 * @param trait
	 *            Trait to search for
	 * @return All NPCs with the given trait
	 */
	public NPC<?>[] getNPCs(Class<? extends Trait> trait);

	/**
	 * Checks whether the given Bukkit entity is an NPC
	 * 
	 * @param livingEntity
	 *            LivingEntity to check
	 * @return Whether the given LivingEntity is an NPC
	 */
	public boolean isNPC(LivingEntity livingEntity);
}