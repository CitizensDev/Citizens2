package net.citizensnpcs.api.npc.character;

import java.util.EnumSet;
import java.util.Set;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Represents a Character that can be loaded and saved. One Character can be
 * attached to an NPC at a time.
 */
public abstract class Character {
    private String name = null;
    private Set<EntityType> types;

    /**
     * Gets the name of this character.
     * 
     * @return Name of this character
     */
    public final String getName() {
        return name;
    }

    /**
     * Gets the valid mob types that this character can be.
     * 
     * @return List of valid mob types
     */
    public final Set<EntityType> getValidTypes() {
        return types;
    }

    /**
     * Loads a trait.
     * 
     * @param key
     *            DataKey to load from
     * @throws NPCLoadException
     *             Thrown if this character fails to load properly
     */
    public abstract void load(DataKey key) throws NPCLoadException;

    /**
     * Called when an NPC is left-clicked.
     * 
     * @param npc
     *            NPC that was left-clicked
     * @param by
     *            Player that clicked the NPC
     */
    public void onLeftClick(NPC npc, Player by) {
    }

    /**
     * Called when this character is removed from an NPC.
     * 
     * @param npc
     *            NPC that had this character removed
     */
    public void onRemove(NPC npc) {
    }

    /**
     * Called when an NPC is right-clicked.
     * 
     * @param npc
     *            NPC that was right-clicked
     * @param by
     *            Player that clicked the NPC
     */
    public void onRightClick(NPC npc, Player by) {
    }

    /**
     * Called when an NPC is set as this character.
     * 
     * @param npc
     *            NPC that is set as this character
     */
    public void onSet(NPC npc) {
    }

    /**
     * Saves a trait.
     * 
     * @param key
     *            DataKey to save to
     */
    public abstract void save(DataKey key);

    public final void setName(String name) {
        if (this.name != null)
            throw new IllegalStateException("Cannot change the name of a character.");

        this.name = name;
    }

    public final void setValidTypes(EntityType... types) {
        if (this.types != null)
            throw new IllegalStateException("Cannot change the valid mob types of a character.");
        if (types.length == 0) {
            this.types = EnumSet.noneOf(EntityType.class);
        } else {
            this.types = EnumSet.of(types[0], types);
        }
    }
}