package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Represents the spawn state of an NPC. This only determines whether an NPC should spawn onEnable. For checking if an
 * NPC's entity is spawned, use NPC.isSpawned().
 */
@TraitName("spawned")
public class Spawned extends Trait {
    @Persist("")
    private boolean shouldSpawn = true;

    public Spawned() {
        super("spawned");
    }

    /**
     * Sets whether an NPC should spawn during server starts or reloads.
     *
     * @param shouldSpawn
     *            Whether an NPC should spawn
     */
    public void setSpawned(boolean shouldSpawn) {
        this.shouldSpawn = shouldSpawn;
    }

    /**
     * Gets whether an NPC should spawn during server starts or reloads.
     *
     * @return Whether an NPC should spawn
     */
    public boolean shouldSpawn() {
        return shouldSpawn;
    }

    @Override
    public String toString() {
        return "Spawned{" + shouldSpawn + "}";
    }
}