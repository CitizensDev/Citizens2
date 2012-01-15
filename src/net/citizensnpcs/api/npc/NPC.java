package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.pathfinding.Navigator;
import net.citizensnpcs.api.npc.trait.Trait;

import org.bukkit.Location;

/**
 * Represents an NPC with a Character and separate traits
 */
public interface NPC {

	/**
	 * Gets the full name of this NPC
	 * 
	 * @return Full name of this NPC
	 */
	public String getFullName();

	/**
	 * Gets the name of this NPC with color codes stripped
	 * 
	 * @return Stripped name of this NPC
	 */
	public String getName();

	/**
	 * Sets the name of this NPC
	 * 
	 * @param name
	 *            Name to give this NPC
	 */
	public void setName(String name);

	/**
	 * Adds a trait to this NPC
	 * 
	 * @param trait
	 *            Trait to add
	 */
	public void addTrait(Trait trait);

	/**
	 * Adds a trait with the given name to this NPC
	 * 
	 * @param trait
	 *            Name of the trait to add
	 */
	public void addTrait(String trait);

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
	 * @param trait
	 *            Name of the trait
	 * @return Trait with the given name
	 */
	public Trait getTrait(String trait);

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
	 * @param trait
	 *            Name of the trait to check
	 * @return Whether this NPC has a trait with the given name
	 */
	public boolean hasTrait(String trait);

	/**
	 * Removes a trait from this NPC
	 * 
	 * @param trait
	 *            Trait to remove
	 */
	public void removeTrait(Trait trait);

	/**
	 * Removes a trait with the given name from this NPC
	 * 
	 * @param trait
	 *            Name of the trait to remove
	 */
	public void removeTrait(String trait);

	/**
	 * Sets the character of this NPC
	 * 
	 * @param character
	 *            Character to set this NPC to
	 */
	public void setCharacter(Character character);

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
	 */
	public void spawn(Location location);

	/**
	 * Despawns this NPC
	 */
	public void despawn();

	/**
	 * Permanently removes this NPC
	 */
	public void remove();
}