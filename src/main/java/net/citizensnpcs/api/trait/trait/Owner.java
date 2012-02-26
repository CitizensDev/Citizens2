package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.SaveId;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents the owner of an NPC
 */
@SaveId("owner")
public class Owner extends Trait {
    private String owner;

    public Owner() {
    }

    public Owner(String owner) {
        this.owner = owner;
    }

    /**
     * Gets the owner of an NPC
     * 
     * @return Name of the owner of an NPC
     */
    public String getOwner() {
        return owner;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        try {
            owner = key.getString("");
        } catch (Exception ex) {
            owner = "notch";
            throw new NPCLoadException("Invalid owner.");
        }
    }

    @Override
    public void save(DataKey key) {
        key.setString("", owner);
    }

    /**
     * Sets the owner of an NPC
     * 
     * @param owner
     *            Name of the player to set as owner of an NPC
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Owner{" + owner + "}";
    }
}