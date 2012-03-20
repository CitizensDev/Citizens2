package net.citizensnpcs.api.trait.trait;

import org.bukkit.entity.Player;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents the owner of an NPC.
 */
public class Owner extends Trait {
    private String owner = "server";

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
     * Gets if the given player is the owner of an NPC.
     * 
     * @param player
     *            Player to check
     * @return Whether the given player is the owner of an NPC
     */
    public boolean isOwner(Player player) {
        return owner.equals(player.getName()) || player.hasPermission("citizens.admin")
                || (owner.equals("server") && player.hasPermission("citizens.admin"));
    }

    /**
     * Gets the owner of an NPC.
     * 
     * @return Name of the owner of an NPC
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the owner of an NPC.
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