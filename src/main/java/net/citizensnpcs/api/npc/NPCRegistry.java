package net.citizensnpcs.api.npc;

import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

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
     * Creates an {@link NPC} with the given id. WARNING: may overwrite any
     * existing NPC in the registry with the same ID.
     *
     * @param type
     *            The {@link EntityType} of the NPC.
     * @param id
     *            The NPC ID
     * @param name
     *            The NPC name
     * @return The created NPC
     */
    public NPC createNPC(EntityType type, UUID uuid, int id, String name);

    /**
     * Deregisters the {@link NPC} and removes all data about it from the data
     * store.
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
     * Gets the {@link NPC} with the given ID if it exists.
     *
     * @param id
     *            ID of the NPC
     * @return NPC with the given ID (may or may not be spawned)
     */
    public NPC getById(int id);

    /**
     * Gets the {@link NPC} with the given unique ID if it exists, otherwise
     * null.
     *
     * @param uuid
     *            ID of the NPC
     * @return NPC with the given UUID
     */
    public NPC getByUniqueId(UUID uuid);

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
     * Returns a <em>sorted</em> view of this registry, sorted by NPC id.
     *
     * @return A sorted view of the registry
     */
    Iterable<NPC> sorted();
}