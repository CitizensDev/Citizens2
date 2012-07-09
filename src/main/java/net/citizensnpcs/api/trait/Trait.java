package net.citizensnpcs.api.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents a Trait that can be loaded and saved.
 */
public abstract class Trait {
    private final String name;
    protected NPC npc = null;

    protected Trait(String name) {
        this.name = name;
    }

    /**
     * Gets the name of this trait.
     * 
     * @return Name of this trait
     */
    public final String getName() {
        return name;
    }

    public NPC getNPC() {
        return this.npc;
    }

    /**
     * Loads a trait.
     * 
     * @param key
     *            DataKey to load from
     * @throws NPCLoadException
     *             Thrown if this trait failed to load properly
     */
    public abstract void load(DataKey key) throws NPCLoadException;

    /**
     * Called when an NPC is spawned. NPCs cannot be physically modified until
     * the entity is created in-game. This is called after the entity has been
     * created.
     */
    public void onNPCSpawn() {
    }

    /**
     * Called when a trait is removed from the given NPC.
     */
    public void onRemove() {
    }

    /**
     * Saves a trait.
     * 
     * @param key
     *            DataKey to save to
     */
    public abstract void save(DataKey key);

    public void setNPC(NPC npc) {
        if (this.npc != null)
            throw new IllegalArgumentException("npc may only be set once");
        this.npc = npc;
    }
}