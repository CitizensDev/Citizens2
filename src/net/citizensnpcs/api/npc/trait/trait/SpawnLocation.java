package net.citizensnpcs.api.npc.trait.trait;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.npc.trait.Trait;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SpawnLocation implements Trait {
    private Location loc;

    public SpawnLocation() {
        // no args constructor
    }

    public SpawnLocation(Location loc) {
        this.loc = loc;
    }

    @Override
    public String getName() {
        return "location";
    }

    @Override
    public void load(DataKey key) {
        loc = new Location(Bukkit.getWorld(key.getString("world")), key.getDouble("x"), key.getDouble("y"),
                key.getDouble("z"), (float) key.getDouble("pitch"), (float) key.getDouble("yaw"));
    }

    @Override
    public void save(DataKey key) {
        key.setString("world", loc.getWorld().getName());
        key.setDouble("x", loc.getX());
        key.setDouble("y", loc.getY());
        key.setDouble("z", loc.getZ());
        key.setDouble("pitch", loc.getPitch());
        key.setDouble("yaw", loc.getYaw());
    }

    public Location getLocation() {
        return loc;
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    @Override
    public String toString() {
        return "SpawnLocation{" + loc + "}";
    }
}