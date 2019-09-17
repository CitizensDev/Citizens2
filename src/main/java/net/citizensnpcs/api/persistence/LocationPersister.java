package net.citizensnpcs.api.persistence;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import net.citizensnpcs.api.util.DataKey;

public class LocationPersister implements Persister<Location> {
    @Override
    public Location create(DataKey root) {
        if (!root.keyExists("world"))
            return null;
        World world = Bukkit.getWorld(root.getString("world"));
        double x = root.getDouble("x"), y = root.getDouble("y"), z = root.getDouble("z");
        float yaw = normalise(root.getDouble("yaw")), pitch = normalise(root.getDouble("pitch"));
        return world == null ? new LazilyLoadedLocation(root.getString("world"), x, y, z, yaw, pitch)
                : new Location(world, x, y, z, yaw, pitch);
    }

    private float normalise(double d) {
        if (Double.isNaN(d)) {
            return 0F;
        }
        return (float) (!Double.isFinite(d) ? 0 : d);
    }

    private double round(double z) {
        if (Double.isInfinite(z) || Double.isNaN(z)) {
            return 0F;
        }
        return new BigDecimal(z).setScale(4, RoundingMode.HALF_DOWN).doubleValue();
    }

    @Override
    public void save(Location location, DataKey root) {
        if (location.getWorld() != null) {
            root.setString("world", location.getWorld().getName());
        }
        root.setDouble("x", round(location.getX()));
        root.setDouble("y", round(location.getY()));
        root.setDouble("z", round(location.getZ()));
        root.setDouble("yaw", round(location.getYaw()));
        root.setDouble("pitch", round(location.getPitch()));
    }

    public static class LazilyLoadedLocation extends Location {
        private final String worldName;

        public LazilyLoadedLocation(String world, double x, double y, double z, float yaw, float pitch) {
            super(null, x, y, z, yaw, pitch);
            this.worldName = world;
        }

        @Override
        public World getWorld() {
            if (super.getWorld() == null) {
                super.setWorld(Bukkit.getWorld(worldName));
            }
            return super.getWorld();
        }
    }
}
