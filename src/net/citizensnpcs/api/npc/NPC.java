package net.citizensnpcs.api.npc;

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
     * Adds a trait to this NPC
     * 
     * @param trait
     *            Trait to add
     */
    public void addTrait(Class<? extends Trait> trait);

    /**
     * Despawns this NPC
     */
    public void despawn();

    /**
     * Gets the character of this NPC
     * 
     * @return Character of this NPC
     */
    public Character getCharacter();

    /**
     * Gets the Bukkit handle of this NPC
     * 
     * @return Bukkit handle of this NPC
     */
    public T getHandle();

    /**
     * Gets the unique ID of this NPC
     * 
     * @return ID of this NPC
     */
    public int getId();

    public Navigator getNavigator();

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
    public boolean hasTrait(Class<? extends Trait> trait);

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
     */
    public void removeTrait(Class<? extends Trait> trait);

    /**
     * Sets the character of this NPC
     * 
     * @param character
     *            Character to set this NPC to
     */
    public void setCharacter(Character character);

    /**
     * Attempts to spawn this NPC
     * 
     * @param location
     *            Location to spawn this NPC
     */
    public void spawn(Location location);
}