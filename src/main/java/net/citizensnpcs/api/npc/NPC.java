package net.citizensnpcs.api.npc;

import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.speech.SpeechController;
import net.citizensnpcs.api.astar.Agent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

/**
 * Represents an NPC with optional {@link Trait}s.
 */
public interface NPC extends Agent {

    /**
     * Adds a trait to this NPC.
     * 
     * @param trait
     *            The class of the trait to add
     */
    public void addTrait(Class<? extends Trait> trait);

    /**
     * Adds a trait to this NPC.
     * 
     * @param trait
     *            Trait to add
     */
    public void addTrait(Trait trait);

    /**
     * @return The metadata store of this NPC.
     */
    public MetadataStore data();

    /**
     * Despawns this NPC.
     * 
     * @return Whether this NPC was able to despawn
     */
    public boolean despawn();

    /**
     * Despawns this NPC.
     * 
     * @param reason
     *            The reason for despawning, for use in {@link NPCDespawnEvent}
     * @return Whether this NPC was able to despawn
     */
    boolean despawn(DespawnReason reason);

    /**
     * Permanently removes this NPC and all data about it from the registry it's
     * attached to.
     */
    public void destroy();

    /**
     * Gets the Bukkit entity associated with this NPC.
     * 
     * @return Entity associated with this NPC
     */
    public LivingEntity getBukkitEntity();

    /**
     * Gets the default {@link GoalController} of this NPC.
     * 
     * @return Default goal controller
     */
    public GoalController getDefaultGoalController();

    /**
     * Gets the default {@link SpeechController} of this NPC.
     * 
     * @return Default speech controller
     */
    public SpeechController getDefaultSpeechController();
    
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
     * @return The {@link Navigator} of this NPC.
     */
    public Navigator getNavigator();

    /**
     * Gets a trait from the given class.
     * 
     * @param trait
     *            Trait to get
     * @return Trait with the given name
     */
    public <T extends Trait> T getTrait(Class<T> trait);

    /**
     * Returns the currently attached {@link Trait}s
     * 
     * @return An Iterable of the current traits
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
     * Removes a trait from this NPC.
     * 
     * @param trait
     *            Trait to remove
     */
    public void removeTrait(Class<? extends Trait> trait);

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

    public static final String DEFAULT_PROTECTED_METADATA = "protected";
}