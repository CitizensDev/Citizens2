package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Represents the owner of an NPC.
 */
public class Owner extends Trait {
    private String owner = SERVER;

    public Owner() {
        super("owner");
    }

    /**
     * Gets the owner.
     * 
     * @return The owner's name
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Gets if the given {@link CommandSender} is the owner of an NPC.
     * 
     * @param sender
     *            Sender to check
     * @return Whether the sender is the owner of an NPC
     */
    public boolean isOwnedBy(CommandSender sender) {
        if (sender instanceof Player) {
            return owner.equalsIgnoreCase(sender.getName()) || sender.hasPermission("citizens.admin")
                    || (owner.equals(SERVER) && sender.hasPermission("citizens.admin"));
        }
        return owner.equals(SERVER);
    }

    public boolean isOwnedBy(String name) {
        return owner.equalsIgnoreCase(name);
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        try {
            owner = key.getString("");
        } catch (Exception ex) {
            owner = SERVER;
            throw new NPCLoadException("Invalid owner.");
        }
    }

    @Override
    public void save(DataKey key) {
        key.setString("", owner);
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

    public static final String SERVER = "server";
}