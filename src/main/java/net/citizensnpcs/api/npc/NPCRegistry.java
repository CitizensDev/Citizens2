package net.citizensnpcs.api.npc;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.event.DespawnReason;

/**
 * Controls the registration and lookup of a set of {@link NPC}s.
 */
public interface NPCRegistry extends Iterable<NPC> {
    /**
     * Creates an despawned {@link NPC}.
     *
     * @param type
     *            {@link EntityType} to assign to the NPC
     * @param name
     *            Name to give the NPC
     * @return Created NPC
     */
    public NPC createNPC(EntityType type, String name);

    /**
     * Creates an spawned {@link NPC} at the given location.
     *
     * @param type
     *            {@link EntityType} to assign to the NPC
     * @param name
     *            Name to give the NPC
     * @param loc
     *            The location to spawn at
     * @return Created NPC
     */
    public NPC createNPC(EntityType type, String name, Location loc);

    /**
     * Creates an {@link NPC} with the given id. WARNING: may overwrite any existing NPC in the registry with the same
     * ID.
     *
     * @param type
     *            The {@link EntityType} of the NPC.
     * @param id
     *            The NPC ID
     * @param name
     *            The NPC name
     * @return The created NPC
     */
    public NPC createNPC(EntityType type, UUID uuid, int id, String name);;

    /**
     * Creates an despawned {@link NPC} using the given ItemStack to configure it if possible.
     *
     * @param type
     *            {@link EntityType} to assign to the NPC
     * @param name
     *            Name to give the NPC
     * @param item
     *            ItemStack to configure with
     * @return Created NPC
     */
    public NPC createNPCUsingItem(EntityType type, String name, ItemStack item);

    /**
     * Deregisters the {@link NPC} and removes all data about it from the data store.
     *
     * @param npc
     *            The NPC to deregister
     */
    public void deregister(NPC npc);

    /**
     * Deregisters all {@link NPC}s from this registry. {@link #deregister(NPC)}
     */
    public void deregisterAll();

    /**
     * Despawn all NPCs within the registry.
     *
     * @param reload
     *            The reason to despawn
     */
    public void despawnNPCs(DespawnReason reason);

    /**
     * Gets the {@link NPC} with the given ID if it exists.
     *
     * @param id
     *            ID of the NPC
     * @return NPC with the given ID (may or may not be spawned)
     */
    public NPC getById(int id);

    /**
     * Gets the {@link NPC} with the given unique ID if it exists.
     *
     * @param uuid
     *            UUID of the NPC
     * @return NPC with the given ID (may or may not be spawned)
     */
    public NPC getByUniqueId(UUID uuid);

    /**
     * Gets the {@link NPC} with the given unique ID if it exists, otherwise null.
     *
     * @param uuid
     *            ID of the NPC
     * @return NPC with the given UUID
     */
    public NPC getByUniqueIdGlobal(UUID uuid);

    /**
     * Gets the name of the registry. Not null.
     */
    public String getName();

    /**
     * Tries to convert the given {@link Entity} to a spawned {@link NPC}.
     *
     * @param entity
     *            Entity to get the NPC from
     * @return NPC from the given entity or null if not found.
     */
    public NPC getNPC(Entity entity);

    /**
     * Checks whether the given {@link Entity} is convertable to an {@link NPC}.
     *
     * @param entity
     *            Entity to check
     * @return Whether the given entity is an NPC
     */
    public boolean isNPC(Entity entity);

    /**
     * Saves the NPCs to the internal {@link NPCDataStore}
     */
    public void saveToStore();

    /**
     * Returns a <em>sorted</em> view of this registry, sorted by NPC id.
     *
     * @return A sorted view of the registry
     */
    Iterable<NPC> sorted();
}