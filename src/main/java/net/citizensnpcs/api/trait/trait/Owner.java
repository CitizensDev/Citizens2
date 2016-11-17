package net.citizensnpcs.api.trait.trait;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents the owner of an NPC.
 */
@TraitName("owner")
public class Owner extends Trait {
    private String owner = SERVER;
    private UUID uuid = null;

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
     * @return The owner's UUID, or <code>null</code> if the owner is the server or a UUID has not been collected for
     *         the owner.
     */
    public UUID getOwnerId() {
        return uuid;
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
            if (owner.equalsIgnoreCase(sender.getName())) {
                if (uuid == null) {
                    uuid = ((Player) sender).getUniqueId();
                } else if (uuid.equals(((Player) sender).getUniqueId())) {
                    return true;
                }
            }
            return sender.hasPermission("citizens.admin")
                    || (owner.equals(SERVER) && sender.hasPermission("citizens.admin"));
        }
        return owner.equals(SERVER);
    }

    public boolean isOwnedBy(String name) {
        return owner.equalsIgnoreCase(name);
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (key.keyExists("owner")) {
            owner = key.getString("owner");
            if (key.keyExists("uuid") && !key.getString("uuid").isEmpty()) {
                uuid = UUID.fromString(key.getString("uuid"));
            }
        } else {
            try {
                owner = key.getString("");
                uuid = null;
            } catch (Exception ex) {
                owner = SERVER;
                uuid = null;
                throw new NPCLoadException("Invalid owner.");
            }
        }
    }

    @Override
    public void save(DataKey key) {
        if (key.getString("") != null && !key.getString("").isEmpty()) {
            key.removeKey("");
        }
        key.setString("owner", owner);
        key.setString("uuid", uuid == null ? "" : uuid.toString());
    }

    public void setOwner(CommandSender sender) {
        this.owner = sender.getName();
        if (sender instanceof Player) {
            this.uuid = ((Player) sender).getUniqueId();
        }
    }

    /**
     * Sets the owner of an NPC.
     *
     * @param owner
     *            Name of the player to set as owner of an NPC
     */
    public void setOwner(String owner) {
        setOwner(owner, null);
    }

    /**
     * Sets the owner of an NPC.
     * 
     * @param owner
     *            Name of the owner
     * @param uuid
     *            UUID of the owner
     */
    public void setOwner(String owner, UUID uuid) {
        this.owner = owner;
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "Owner{" + owner + "}";
    }

    public static final String SERVER = "server";
}