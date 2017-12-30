package net.citizensnpcs.trait;

import org.bukkit.Location;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("location")
public class CurrentLocation extends Trait {
    @Persist(value = "", required = true)
    private Location location = new Location(null, 0, 0, 0);

    public CurrentLocation() {
        super("location");
    }

    public Location getLocation() {
        return location.getWorld() == null ? null : location;
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        location = npc.getEntity().getLocation(location);
    }

    public void setLocation(Location loc) {
        this.location = loc.clone();
    }

    @Override
    public String toString() {
        return "CurrentLocation{" + location + "}";
    }
}