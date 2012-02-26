package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.SaveId;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents the spawn state of an NPC. This only determines whether an NPC
 * should spawn onEnable. For checking if an NPC's entity is spawned, use
 * NPC.isSpawned().
 */
@SaveId("spawned")
public class Spawned extends Trait {
    private boolean shouldSpawn;

    public Spawned() {
    }

    public Spawned(boolean shouldSpawn) {
        this.shouldSpawn = shouldSpawn;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        try {
            shouldSpawn = key.getBoolean("");
        } catch (Exception ex) {
            shouldSpawn = false;
            throw new NPCLoadException("Invalid value. Valid values: true or false");
        }
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", shouldSpawn);
    }

    /**
     * Sets whether an NPC should spawn during server starts or reloads
     * 
     * @param shouldSpawn
     *            Whether an NPC should spawn
     */
    public void setSpawned(boolean shouldSpawn) {
        this.shouldSpawn = shouldSpawn;
    }

    /**
     * Gets whether an NPC should spawn during server starts or reloads
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