package net.citizensnpcs.api.npc;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import net.citizensnpcs.api.npc.character.Character;

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
	 * Attempts to spawn an NPC at the given location
	 * 
	 * @param npc
	 *            NPC to spawn
	 * @param location
	 *            Location to spawn the NPC
	 * @return Whether the NPC was able to spawn based on its spawning
	 *         conditions
	 */
	public boolean spawnNPC(NPC<?> npc, Location location);

	/**
	 * Attempts to spawn an NPC at the given location
	 * 
	 * @param npc
	 *            NPC to spawn
	 * @param location
	 *            Location to spawn the NPC
	 * @param force
	 *            Whether to force this NPC to spawn even if it does not meet
	 *            its spawning conditions
	 * @return Whether the NPC was able to spawn based on its spawning
	 *         conditions
	 */
	public boolean spawnNPC(NPC<?> npc, Location location, boolean force);

	/**
	 * Despawns an NPC
	 * 
	 * @param npc
	 *            NPC to despawn
	 */
	public void despawnNPC(NPC<?> npc);

	/**
	 * Attempts to despawn an NPC
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
}