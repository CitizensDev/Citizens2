package net.citizensnpcs.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class CurrentLocation extends Trait implements Runnable {
    private Location loc;
    private final NPC npc;

    public CurrentLocation(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void run() {
        if (npc.getBukkitEntity() == null)
            return;

        loc = npc.getBukkitEntity().getLocation();
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (Bukkit.getWorld(key.getString("world")) == null)
            throw new NPCLoadException("'" + key.getString("world") + "' is not a valid world.");

        loc = new Location(Bukkit.getWorld(key.getString("world")), key.getDouble("x"), key.getDouble("y"), key
                .getDouble("z"), (float) key.getDouble("yaw"), (float) key.getDouble("pitch"));
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

    public Location getLocation() {
        return loc;
    }

    public void spawn(Location loc) {
        this.loc = loc;
    }

    @Override
    public String toString() {
        return "SpawnLocation{" + loc + "}";
    }
}