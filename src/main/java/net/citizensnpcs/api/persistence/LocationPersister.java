package net.citizensnpcs.api.persistence;

import net.citizensnpcs.api.util.DataKey;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationPersister implements Persister<Location> {
    @Override
    public Location create(DataKey root) {
        if (!root.keyExists("world"))
            return null;
        World world = Bukkit.getWorld(root.getString("world"));
        double x = root.getDouble("x"), y = root.getDouble("y"), z = root.getDouble("z");
        float yaw = (float) root.getDouble("yaw"), pitch = (float) root.getDouble("pitch");
        return world == null ? new LazilyLoadedLocation(root.getString("world"), x, y, z, yaw, pitch) : new Location(
                world, x, y, z, yaw, pitch);
    }

    @Override
    public void save(Location location, DataKey root) {
        if (location.getWorld() != null)
            root.setString("world", location.getWorld().getName());
        root.setDouble("x", location.getX());
        root.setDouble("y", location.getY());
        root.setDouble("z", location.getZ());
        root.setDouble("yaw", location.getYaw());
        root.setDouble("pitch", location.getPitch());
    }

    public static class LazilyLoadedLocation extends Location {
        private final String worldName;

        public LazilyLoadedLocation(String world, double x, double y, double z, float yaw, float pitch) {
            super(null, x, y, z, yaw, pitch);
            this.worldName = world;
        }

        @Override
        public World getWorld() {
            if (super.getWorld() == null)
                super.setWorld(Bukkit.getWorld(worldName));
            return super.getWorld();
        }
    }
}
