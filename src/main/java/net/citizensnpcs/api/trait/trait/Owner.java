package net.citizensnpcs.api.trait.trait;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;

/**
 * Represents the owner of an NPC.
 */
@TraitName("owner")
public class Owner extends Trait {
    private UUID uuid = null;

    public Owner() {
        super("owner");
    }

    /**
     * Gets the owner.
     *
     * @return "SERVER" or the UUID string
     */
    @Deprecated
    public String getOwner() {
        return uuid == null ? "SERVER" : uuid.toString();
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
        if (sender == null)
            return false;
        if (uuid == null && sender instanceof ConsoleCommandSender)
            return true;
        if (sender instanceof OfflinePlayer && (uuid != null && uuid.equals(((OfflinePlayer) sender).getUniqueId()))) {
            return true;
        }
        return sender.hasPermission("citizens.admin") || (uuid == null && sender.hasPermission("citizens.admin"));
    }

    public boolean isOwnedBy(String name) {
        return uuid == null ? "SERVER".equals(name) : uuid != null && uuid.toString().equalsIgnoreCase(name);
    }

    public boolean isOwnedBy(UUID other) {
        return uuid == null ? other == null : uuid.equals(other);
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (key.keyExists("uuid") && !key.getString("uuid").isEmpty()) {
            uuid = UUID.fromString(key.getString("uuid"));
        } else {
            uuid = null;
        }
        key.removeKey("owner");
    }

    @Override
    public void save(DataKey key) {
        key.setString("uuid", uuid == null ? "" : uuid.toString());
    }

    public void setOwner(CommandSender sender) {
        if (sender instanceof OfflinePlayer) {
            this.uuid = ((OfflinePlayer) sender).getUniqueId();
        } else {
            this.uuid = null;
        }
    }

    /**
     * Sets the owner of an NPC.
     *
     * @param owner
     *            Name of the player to set as owner of an NPC
     */
    @Deprecated
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
    @Deprecated
    public void setOwner(String owner, UUID uuid) {
        this.uuid = uuid;
    }

    public void setOwner(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "Owner{" + uuid + "}";
    }
}