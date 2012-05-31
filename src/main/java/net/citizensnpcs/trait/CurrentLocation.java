package net.citizensnpcs.trait;

import net.citizensnpcs.api.abstraction.WorldVector;
import net.citizensnpcs.api.attachment.Attachment;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;

public class CurrentLocation extends Attachment implements Runnable {
    private WorldVector loc;
    private final NPC npc;

    public CurrentLocation(NPC npc) {
        this.npc = npc;
    }

    public WorldVector getLocation() {
        return loc;
    }

    public void setLocation(WorldVector loc) {
        this.loc = loc;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        if (Bukkit.getWorld(key.getString("world")) == null)
            throw new NPCLoadException("'" + key.getString("world") + "' is not a valid world.");

        loc = new Location(Bukkit.getWorld(key.getString("world")), key.getDouble("x"), key.getDouble("y"),
                key.getDouble("z"), (float) key.getDouble("yaw"), (float) key.getDouble("pitch"));
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;

        loc = npc.getEntity().getLocation();
    }

    @Override
    public void save(DataKey key) {
        if (loc == null) {
            key.removeKey(getName());
            return;
        }

        key.setString("world", loc.getWorld().getName());
        key.setDouble("x", loc.getX());
        key.setDouble("y", loc.getY());
        key.setDouble("z", loc.getZ());
        key.setDouble("yaw", loc.getYaw());
        key.setDouble("pitch", loc.getPitch());
    }

    @Override
    public String toString() {
        return "CurrentLocation{" + loc + "}";
    }
}