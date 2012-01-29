package net.citizensnpcs.api.npc.trait.trait;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.npc.trait.Trait;

public class Spawned implements Trait {
    private boolean spawned;

    public Spawned() {
    }

    public Spawned(boolean spawned) {
        this.spawned = spawned;
    }

    @Override
    public String getName() {
        return "spawned";
    }

    @Override
    public void load(DataKey key) {
        spawned = key.getBoolean("");
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("", spawned);
    }

    /**
     * Gets whether an NPC should spawn during server starts or reloads
     * 
     * @return Whether an NPC should spawn
     */
    public boolean isSpawned() {
        return spawned;
    }

    /**
     * Sets whether an NPC should spawn during server starts or reloads
     * 
     * @param spawned
     *            Whether an NPC should spawn
     */
    public void setSpawned(boolean spawned) {
        this.spawned = spawned;
    }
}