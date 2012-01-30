package net.citizensnpcs.api.npc.trait.trait;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.npc.trait.Trait;

public class Spawned implements Trait {
    private boolean shouldSpawn;

    public Spawned() {
    }

    public Spawned(boolean shouldSpawn) {
        this.shouldSpawn = shouldSpawn;
    }

    @Override
    public String getName() {
        return "spawned";
    }

    @Override
    public void load(DataKey key) {
        shouldSpawn = key.getBoolean("");
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", shouldSpawn);
    }

    /**
     * Gets whether an NPC should spawn during server starts or reloads
     * 
     * @return Whether an NPC should spawn
     */
    public boolean shouldSpawn() {
        return shouldSpawn;
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
}