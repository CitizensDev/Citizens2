package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.ai.AI;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.metadata.Metadatable;

/**
 * Represents an NPC with a Character and separate traits.
 */
public interface NPC extends Metadatable, InventoryHolder {

    /**
     * Adds a trait to this NPC.
     * 
     * @param trait
     *            Class of the trait to add
     */
    public void addTrait(Class<? extends Trait> trait);

    /**
     * Sends a message to the given player with the NPC's formatted name.
     * 
     * @param player
     *            Player to send message to
     * @param message
     *            Message to send
     */
    public void chat(Player player, String message);

    /**
     * Sends a message to all players online with this NPC's formatted name.
     * 
     * @param message
     *            Message to send
     */
    public void chat(String message);

    /**
     * Despawns this NPC.
     * 
     * @return Whether this NPC was able to despawn
     */
    public boolean despawn();

    /**
     * Gets the {@link AI} of this NPC.
     * 
     * @return AI of this NPC
     */
    public AI getAI();

    /**
     * Gets the Bukkit entity associated with this NPC.
     * 
     * @return Entity associated with this NPC
     */
    public LivingEntity getBukkitEntity();

    /**
     * Gets the character of this NPC.
     * 
     * @return Character of this NPC
     */
    public Character getCharacter();

    /**
     * Gets the full name of this NPC.
     * 
     * @return Full name of this NPC
     */
    public String getFullName();

    /**
     * Gets the unique ID of this NPC.
     * 
     * @return ID of this NPC
     */
    public int getId();

    /**
     * Gets the name of this NPC with color codes stripped.
     * 
     * @return Stripped name of this NPC
     */
    public String getName();

    /**
     * Gets a trait from the given class.
     * 
     * @param trait
     *            Trait to get
     * @return Trait with the given name
     */
    public <T extends Trait> T getTrait(Class<T> trait);

    /**
     * Gets the traits of this NPC, these are not attached to any character.
     * 
     * @return The traits of this NPC
     */
    public Iterable<Trait> getTraits();

    /**
     * Checks if this NPC has the given trait.
     * 
     * @param trait
     *            Trait to check
     * @return Whether this NPC has the given trait
     */
    public boolean hasTrait(Class<? extends Trait> trait);

    /**
     * Gets whether this NPC is currently spawned.
     * 
     * @return Whether this NPC is spawned
     */
    public boolean isSpawned();

    /**
     * Permanently removes this NPC.
     */
    public void remove();

    /**
     * Removes a trait from this NPC.
     * 
     * @param trait
     *            Trait to remove
     */
    public void removeTrait(Class<? extends Trait> trait);

    /**
     * Sets the character of this NPC.
     * 
     * @param character
     *            Character to set this NPC to
     */
    public void setCharacter(Character character);

    /**
     * Sets the name of this NPC.
     * 
     * @param name
     *            Name to give this NPC
     */
    public void setName(String name);

    /**
     * Attempts to spawn this NPC.
     * 
     * @param location
     *            Location to spawn this NPC
     * @return Whether this NPC was able to spawn at the location
     */
    public boolean spawn(Location location);
}