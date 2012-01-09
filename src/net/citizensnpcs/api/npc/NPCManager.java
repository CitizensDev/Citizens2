package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.npc.trait.Character;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface NPCManager<T extends NPC<?>> {
	/**
	 * Creates an NPC with no attached characters or traits
	 * 
	 * @return Created NPC
	 */
	public T createNPC();

	/**
	 * Creates an NPC with the given NPC character
	 * 
	 * @param character
	 *            Character to attach to an NPC
	 * @return Created NPC with given NPC character
	 */
	public T createNPC(Class<? extends Character> character);

	/**
	 * Spawns an NPC at the given location
	 * 
	 * @param npc
	 *            NPC to spawn
	 * @param location
	 *            Location to spawn the NPC
	 */
	public void spawnNPC(T npc, Location location);

	/**
	 * Despawns an NPC
	 * 
	 * @param npc
	 *            NPC to despawn
	 */
	public void despawnNPC(T npc);

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
	public T getNPC(int id);

	/**
	 * Gets an NPC from the given LivingEntity
	 * 
	 * @param livingEntity
	 *            LivingEntity to get the NPC from
	 * @return NPC from the given LivingEntity, null if LivingEntity is not an
	 *         NPC
	 */
	public T getNPC(LivingEntity livingEntity);
}