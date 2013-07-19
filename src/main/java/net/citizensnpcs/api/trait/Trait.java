package net.citizensnpcs.api.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.event.Listener;

/**
 * Represents a Trait that can be loaded and saved.
 */
public abstract class Trait implements Listener, Runnable {
    private final String name;
    protected NPC npc = null;
    private boolean runImplemented = true;

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

    /**
     * @return The {@link NPC} this trait is attached to. May be null.
     */
    public final NPC getNPC() {
        return npc;
    }

    public boolean isRunImplemented() {
        run();
        return runImplemented;
    }

    public void linkToNPC(NPC npc) {
        if (this.npc != null)
            throw new IllegalArgumentException("npc may only be set once");
        this.npc = npc;
        onAttach();
    }

    /**
     * Loads a trait.
     * 
     * @param key
     *            DataKey to load from
     * @throws NPCLoadException
     *             Thrown if this trait failed to load properly
     */
    public void load(DataKey key) throws NPCLoadException {
    }

    /**
     * Called when the trait has been attached to an {@link NPC}. {@link #npc}
     * will be null until this is called.
     */
    public void onAttach() {
    }

    /**
     * Called when the trait has been newly copied to an {@link NPC}.
     */
    public void onCopy() {
    }

    /**
     * Called just before the attached {@link NPC} is despawned.
     * {@link NPC#getBukkitEntity()} will be non-null.
     */
    public void onDespawn() {
    }

    /**
     * Called when a trait is removed from the attached {@link NPC}.
     */
    public void onRemove() {
    }

    /**
     * Called when an {@link NPC} is spawned. {@link NPC#getBukkitEntity()} will
     * return null until this is called. This is also called onAttach when the
     * NPC is already spawned.
     */
    public void onSpawn() {
    }

    @Override
    public void run() {
        runImplemented = false;
    }

    /**
     * Saves a trait.
     * 
     * @param key
     *            DataKey to save to
     */
    public void save(DataKey key) {
    }
}