package net.citizensnpcs.api.npc;

import java.util.Set;

import net.citizensnpcs.api.npc.character.Character;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public interface NPC<T extends LivingEntity> {

	/**
	 * Gets the unique ID of this NPC
	 * 
	 * @return ID of this NPC
	 */
	public int getId();

	/**
	 * Gets the characters of this NPC
	 * 
	 * @return Set of registered characters
	 */
	public Set<Character> getCharacters();

	/**
	 * Adds a character to this NPC
	 * 
	 * @param character
	 *            Character to add
	 */
	public void addCharacter(Class<? extends Character> character);

	/**
	 * Removes a character from this NPC
	 * 
	 * @param character
	 *            Character to remove
	 */
	public void removeCharacter(Class<? extends Character> character);

	/**
	 * Attempts to spawn this NPC
	 * 
	 * @param location
	 *            Location to spawn this NPC
	 */
	public void spawn(Location location);

	/**
	 * Attempts to spawn this NPC
	 * 
	 * @param location
	 * @param force
	 *            Whether to force this NPC to spawn even if it does not meet
	 *            its spawning conditions
	 * @return Whether this NPC was able to spawn based on its spawning
	 *         conditions
	 */
	public boolean spawn(Location location, boolean force);

	/**
	 * Despawns this NPC
	 */
	public void despawn();
}