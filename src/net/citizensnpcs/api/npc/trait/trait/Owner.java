package net.citizensnpcs.api.npc.trait.trait;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.npc.trait.Trait;

public class Owner implements Trait {
    private String owner;

    public Owner() {
    }

    public Owner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getName() {
        return "owner";
    }

    @Override
    public void load(DataKey key) {
        owner = key.getString("");
    }

    @Override
    public void save(DataKey key) {
        key.setString("", owner);
    }

    /**
     * Gets the owner of an NPC
     * 
     * @return Name of the owner of an NPC
     */
    public String getOwner() {
        return owner;
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
}