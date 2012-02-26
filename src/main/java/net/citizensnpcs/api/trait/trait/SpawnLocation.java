package net.citizensnpcs.api.trait.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.SaveId;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Represents the spawn location of an NPC. This is not reliable for determining
 * an NPC's location. Use NPC.getBukkitEntity().getLocation().
 */
@SaveId("location")
public class SpawnLocation extends Trait {
    private Location loc;

    public SpawnLocation() {
    }

    public SpawnLocation(Location loc) {
        this.loc = loc;
    }

    /**
     * Gets the location where an NPC should spawn upon loading
     * 
     * @return NPC's spawn location
     */
    public Location getLocation() {
        return loc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (Bukkit.getWorld(key.getString("world")) == null)
            throw new NPCLoadException("'" + key.getString("world") + "' is not a valid world.");

        try {
            loc = new Location(Bukkit.getWorld(key.getString("world")), key.getDouble("x"), key.getDouble("y"), key
                    .getDouble("z"), (float) key.getDouble("yaw"), (float) key.getDouble("pitch"));
        } catch (Exception ex) {
            throw new NPCLoadException("Invalid coordinates.");
        }
    }

    @Override
    public void save(DataKey key) {
        key.setString("world", loc.getWorld().getName());
        key.setDouble("x", loc.getX());
        key.setDouble("y", loc.getY());
        key.setDouble("z", loc.getZ());
        key.setDouble("yaw", loc.getYaw());
        key.setDouble("pitch", loc.getPitch());
    }

    /**
     * Sets the location where an NPC should spawn upon loading
     * 
     * @param loc
     *            Location to spawn
     */
    public void setLocation(Location loc) {
        this.loc = loc;
    }

    @Override
    public String toString() {
        return "SpawnLocation{" + loc + "}";
    }
}