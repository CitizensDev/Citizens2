package net.citizensnpcs.api.npc.trait.trait;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.npc.trait.Trait;

public class LocationTrait implements Trait {
    private Location loc;

    @Override
    public String getName() {
        return "location";
    }

    @Override
    public void load(DataKey key) {
        loc = new Location(Bukkit.getWorld(key.getString("location.world")), key.getDouble("location.x"),
                key.getDouble("location.y"), key.getDouble("location.z"), (float) key.getDouble("location.pitch"),
                (float) key.getDouble("location.yaw"));
    }

    @Override
    public void save(DataKey key) {
        key.setString("location.world", loc.getWorld().getName());
        key.setDouble("location.x", loc.getX());
        key.setDouble("location.y", loc.getY());
        key.setDouble("location.z", loc.getZ());
        key.setDouble("location.pitch", loc.getPitch());
        key.setDouble("location.yaw", loc.getYaw());
    }

    public Location getLocation() {
        return loc;
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    @Override
    public String toString() {
        return "LocationTrait{" + loc + "}";
    }
}