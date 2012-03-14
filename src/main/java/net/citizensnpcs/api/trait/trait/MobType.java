package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents an NPC's mob type.
 */
public class MobType extends Trait {
    private String type;

    @Override
    public void load(DataKey key) {
        try {
            type = key.getString("").toUpperCase();
        } catch (IllegalArgumentException ex) {
            type = "PLAYER";
        }
    }

    @Override
    public void save(DataKey key) {
        key.setString("", type);
    }

    /**
     * Gets the type of mob that an NPC is.
     * 
     * @return Name of the mob type of an NPC
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of mob that an NPC is.
     * 
     * @param type
     *            Mob type to set the NPC as
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MobType{" + type + "}";
    }
}