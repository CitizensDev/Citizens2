package net.citizensnpcs.trait;

import org.bukkit.Location;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.NMS;

/**
 * Persists the current {@link Location} of the {@link NPC}. Will cache last known location if despawned.
 */
@TraitName("location")
public class CurrentLocation extends Trait {
    @Persist
    private float bodyYaw = Float.NaN;
    @Persist(value = "", required = true)
    private Location location = new Location(null, 0, 0, 0);

    public CurrentLocation() {
        super("location");
    }

    public float getBodyYaw() {
        return bodyYaw;
    }

    public Location getLocation() {
        return location.getWorld() == null ? null : location.clone();
    }

    @Override
    public void load(DataKey key) {
        key.removeKey("headYaw");
    }

    @Override
    public void onSpawn() {
        if (!Float.isNaN(bodyYaw)) {
            NMS.setBodyYaw(npc.getEntity(), bodyYaw);
        }
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        location = npc.getEntity().getLocation(location);
        bodyYaw = NMS.getYaw(npc.getEntity());
    }

    public void setLocation(Location loc) {
        location = loc.clone();
    }

    @Override
    public String toString() {
        return "CurrentLocation{" + location + "}";
    }
}