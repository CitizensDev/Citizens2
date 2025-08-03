package net.citizensnpcs.api.trait;

import java.util.Locale;

import org.bukkit.event.Listener;

import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.RemoveReason;

/**
 * Represents a Trait linked to an {@link NPC} that can be loaded and saved. This will be kept persisted inside a
 * {@link NPC} across server restarts. Traits must be registered in Citizens' {@link TraitFactory}.
 * <p>
 * All traits should have a default constructor with no arguments for persistence purposes.
 */
public abstract class Trait implements Listener, Runnable {
    private final String name;
    protected NPC npc = null;
    private boolean runImplemented = true;

    protected Trait(String name) {
        this.name = name.toLowerCase(Locale.ROOT);
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
     * Called when the trait has been attached to an {@link NPC}. {@link #npc} will be null until this is called.
     */
    public void onAttach() {
    }

    /**
     * Called when the trait has been newly copied to an {@link NPC}.
     */
    public void onCopy() {
    }

    /**
     * Called just before the attached {@link NPC} is despawned. {@link NPC#getEntity()} will be non-null.
     */
    public void onDespawn() {
    }

    /**
     * Called just before the attached {@link NPC} is despawned. {@link NPC#getEntity()} will be non-null.
     */
    public void onDespawn(DespawnReason reason) {
        onDespawn();
    }

    /**
     * Called just before the {@link NPC} is spawned. {@link NPC#getEntity()} will return an <em>unspawned</em> entity.
     */
    public void onPreSpawn() {

    }

    /**
     * Called when a trait is removed from the attached {@link NPC}.
     */
    public void onRemove() {
    }

    /**
     * Called when a trait is removed from the attached {@link NPC}.
     */
    public void onRemove(RemoveReason reason) {
        onRemove();
    }

    /**
     * Called when an {@link NPC} is spawned. {@link NPC#getEntity()} will return null until this is called. This is
     * also called onAttach when the NPC is already spawned.
     */
    public void onSpawn() {
    }

    /**
     * Called every tick if overridden.
     */
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