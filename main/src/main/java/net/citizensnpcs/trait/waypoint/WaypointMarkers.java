package net.citizensnpcs.trait.waypoint;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;

public class WaypointMarkers {
    private final Map<Waypoint, Entity> waypointMarkers = Maps.newHashMap();
    private final World world;

    public WaypointMarkers(World world) {
        this.world = world;
    }

    public Entity createWaypointMarker(Waypoint waypoint) {
        Entity entity = spawnMarker(world, waypoint.getLocation().clone().add(0, 1, 0));
        if (entity == null)
            return null;
        waypointMarkers.put(waypoint, entity);
        return entity;
    }

    public void destroyWaypointMarkers() {
        for (Entity entity : waypointMarkers.values()) {
            entity.remove();
        }
        waypointMarkers.clear();
    }

    public void removeWaypointMarker(Waypoint waypoint) {
        Entity entity = waypointMarkers.remove(waypoint);
        if (entity != null) {
            entity.remove();
        }
    }

    public Entity spawnMarker(World world, Location at) {
        NPC npc = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore()).createNPC(EntityType.ENDER_SIGNAL,
                "");
        npc.spawn(at);
        return npc.getEntity();
    }
}