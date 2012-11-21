package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

import org.bukkit.Location;

public class CurrentLocation extends Trait {
    @Persist(value = "", required = true)
    private Location loc;

    public CurrentLocation() {
        super("location");
    }

    public Location getLocation() {
        if (loc.getWorld() == null)
            return null;
        return loc;
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        loc = npc.getBukkitEntity().getLocation();
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    @Override
    public String toString() {
        return "CurrentLocation{" + loc + "}";
    }
}