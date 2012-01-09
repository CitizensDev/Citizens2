package net.citizensnpcs.api.npc;

import java.util.Set;

import net.citizensnpcs.api.npc.trait.Trait;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * Represents an NPC with a Character and separate traits
 * 
 * @param <T>
 *            Type of Bukkit entity that this NPC is
 */
public interface NPC<T extends LivingEntity> {

	/**
	 * Gets the unique ID of this NPC
	 * 
	 * @return ID of this NPC
	 */
	public int getId();

	/**
	 * Gets the character of this NPC
	 * 
	 * @return Character of this NPC
	 */
	public Character getCharacter();

	/**
	 * Sets the character of this NPC
	 * 
	 * @param character
	 *            Character to set this NPC to
	 */
	public void setCharacter(Character character);

	/**
	 * Gets the traits of this NPC, these are not attached to any character
	 * 
	 * @return Set of traits of this NPC
	 */
	public Set<Trait> getTraits();

	/**
	 * Adds a trait to this NPC
	 * 
	 * @param trait
	 *            Trait to add
	 */
	public void addTrait(Class<? extends Trait> trait);

	/**
	 * Removes a trait from this NPC
	 * 
	 * @param trait
	 *            Trait to remove
	 */
	public void removeTrait(Class<? extends Trait> trait);

	/**
	 * Attempts to spawn this NPC
	 * 
	 * @param location
	 *            Location to spawn this NPC
	 */
	public void spawn(Location location);

	/**
	 * Despawns this NPC
	 */
	public void despawn();

	/**
	 * Gets the Bukkit handle of this NPC
	 * 
	 * @return Bukkit handle of this NPC
	 */
	public T getHandle();
}