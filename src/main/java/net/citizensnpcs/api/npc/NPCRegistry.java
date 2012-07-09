package net.citizensnpcs.api.npc;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/**
 * Handles various NPC-related methods.
 */
public interface NPCRegistry extends Iterable<NPC> {

    /**
     * Creates an NPC with no attached character. This does not spawn the NPC.
     * 
     * @param type
     *            Entity type to assign to the NPC
     * @param name
     *            Name to give the NPC
     * @return Created NPC
     */
    public NPC createNPC(EntityType type, String name);

    /**
     * Deregisters the {@link NPC} and removes all data about it from the data
     * store.
     * 
     * @param npc
     */
    public void deregister(NPC npc);

    /**
     * Deregisters all {@link NPC}s from this registry. {@link #deregister(NPC)}
     */
    public void deregisterAll();

    /**
     * Gets an NPC with the given ID.
     * 
     * @param id
     *            ID of the NPC
     * @return NPC with the given ID (may or may not be spawned)
     */
    public NPC getById(int id);

    /**
     * Gets an NPC from the given LivingEntity.
     * 
     * @param entity
     *            Entity to get the NPC from
     * @return NPC from the given entity (must be spawned)
     */
    public NPC getNPC(Entity entity);

    /**
     * Checks whether the given Bukkit entity is an NPC.
     * 
     * @param entity
     *            Entity to check
     * @return Whether the given entity is an NPC
     */
    public boolean isNPC(Entity entity);
}