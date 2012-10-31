package net.citizensnpcs.api.persistence;

import net.citizensnpcs.api.util.DataKey;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationPersister implements Persister {
    @Override
    public Object create(DataKey root) {
        if (!root.keyExists("world"))
            return null;
        World world = Bukkit.getWorld(root.getString("world"));
        if (world == null)
            return null;
        return new Location(world, root.getDouble("x"), root.getDouble("y"), root.getDouble("z"),
                (float) root.getDouble("yaw"), (float) root.getDouble("pitch"));
    }

    @Override
    public void save(Object instance, DataKey root) {
        Location location = (Location) instance;
        if (location.getWorld() != null)
            root.setString("world", location.getWorld().getName());
        root.setDouble("x", location.getX());
        root.setDouble("y", location.getY());
        root.setDouble("z", location.getZ());
        root.setDouble("yaw", location.getYaw());
        root.setDouble("pitch", location.getPitch());
    }
}
