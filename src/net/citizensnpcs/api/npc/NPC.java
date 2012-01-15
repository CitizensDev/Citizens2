package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.exception.NPCException;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.pathfinding.Navigator;
import net.citizensnpcs.api.npc.trait.Trait;

import org.bukkit.Location;

/**
 * Represents an NPC with a Character and separate traits
 */
public interface NPC {

	/**
	 * Adds a trait to this NPC
	 * 
	 * @param trait
	 *            Trait to add
	 * @throws NPCException
	 *             Thrown if the trait is already attached to this NPC
	 */
	public void addTrait(Trait trait) throws NPCException;

	/**
	 * Adds a trait with the given name to this NPC
	 * 
	 * @param name
	 *            Name of the trait to add
	 * @throws NPCException
	 *             Thrown if the trait is already attached to this NPC
	 */
	public void addTrait(String name) throws NPCException;

	/**
	 * Gets the character of this NPC
	 * 
	 * @return Character of this NPC
	 */
	public Character getCharacter();

	/**
	 * Gets the unique ID of this NPC
	 * 
	 * @return ID of this NPC
	 */
	public int getId();

	/**
	 * Gets the Navigator of this NPC
	 * 
	 * @return Navigator of this NPC
	 */
	public Navigator getNavigator();

	/**
	 * Gets a trait with the given name for this NPC
	 * 
	 * @param name
	 *            Name of the trait
	 * @return Trait with the given name
	 */
	public Trait getTrait(String name);

	/**
	 * Gets the traits of this NPC, these are not attached to any character
	 * 
	 * @return The traits of this NPC
	 */
	public Iterable<Trait> getTraits();

	/**
	 * Checks if this NPC has the given trait
	 * 
	 * @param trait
	 *            Trait to check
	 * @return Whether this NPC has the given trait
	 */
	public boolean hasTrait(Trait trait);

	/**
	 * Checks if this NPC has the given trait
	 * 
	 * @param name
	 *            Name of the trait to check
	 * @return Whether this NPC has a trait with the given name
	 */
	public boolean hasTrait(String name);

	/**
	 * Removes a trait from this NPC
	 * 
	 * @param trait
	 *            Trait to remove
	 * @throws NPCException
	 *             Thrown if the given trait is not attached to this NPC
	 */
	public void removeTrait(Trait trait) throws NPCException;

	/**
	 * Removes a trait with the given name from this NPC
	 * 
	 * @param name
	 *            Name of the trait to remove
	 * @throws NPCException
	 *             Thrown if the given trait is not attached to this NPC
	 */
	public void removeTrait(String name) throws NPCException;

	/**
	 * Sets the character of this NPC
	 * 
	 * @param character
	 *            Character to set this NPC to
	 * @throws NPCException
	 *             Thrown if this NPC already is the given character
	 */
	public void setCharacter(Character character) throws NPCException;

	/**
	 * Gets whether this NPC is currently spawned
	 * 
	 * @return Whether this NPC is spawned
	 */
	public boolean isSpawned();

	/**
	 * Attempts to spawn this NPC
	 * 
	 * @param location
	 *            Location to spawn this NPC
	 * @throws NPCException
	 *             Thrown if this NPC is already spawned
	 */
	public void spawn(Location location) throws NPCException;

	/**
	 * Despawns this NPC
	 * 
	 * @throws NPCException
	 *             Thrown if this NPC is already despawned
	 */
	public void despawn() throws NPCException;

	/**
	 * Permanently removes this NPC
	 */
	public void remove();
}