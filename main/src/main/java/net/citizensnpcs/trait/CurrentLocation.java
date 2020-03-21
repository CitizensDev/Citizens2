package net.citizensnpcs.trait;

import org.bukkit.Location;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.util.NMS;

/**
 * Persists the current {@link Location} of the {@link NPC}. Will cache last known location if despawned.
 */
@TraitName("location")
public class CurrentLocation extends Trait {
    @Persist
    private float headYaw;
    @Persist(value = "", required = true)
    private Location location = new Location(null, 0, 0, 0);

    public CurrentLocation() {
        super("location");
    }

    public float getHeadYaw() {
        return headYaw;
    }

    public Location getLocation() {
        return location.getWorld() == null ? null : location;
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        location = npc.getEntity().getLocation(location);
        headYaw = NMS.getHeadYaw(npc.getEntity());
    }

    public void setLocation(Location loc) {
        this.location = loc.clone();
    }

    @Override
    public String toString() {
        return "CurrentLocation{" + location + "}";
    }
}