package net.citizensnpcs.api.npc.trait.trait;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.npc.trait.Trait;

public class MobType implements Trait {
    private String type;

    public MobType() {
    }

    public MobType(String type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return "type";
    }

    @Override
    public void load(DataKey key) {
        type = key.getString("").toUpperCase();
    }

    @Override
    public void save(DataKey key) {
        key.setString("", type);
    }

    /**
     * Gets the type of creature that an NPC is
     * 
     * @return Name of the creature type of an NPC
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of creature that an NPC is
     * 
     * @param type
     *            Name of the creature type to set an NPC as
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MobType{" + type + "}";
    }
}