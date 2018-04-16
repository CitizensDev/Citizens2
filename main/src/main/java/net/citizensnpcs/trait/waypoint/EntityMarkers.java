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
import net.citizensnpcs.api.npc.NPCRegistry;

public class EntityMarkers<T> {
    private final Map<T, Entity> markers = Maps.newHashMap();
    private final NPCRegistry registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());

    public Entity createMarker(T marker, Location at) {
        Entity entity = spawnMarker(at.getWorld(), at);
        if (entity == null)
            return null;
        markers.put(marker, entity);
        return entity;
    }

    public void destroyMarkers() {
        for (Entity entity : markers.values()) {
            entity.remove();
        }
        markers.clear();
    }

    public void removeMarker(T marker) {
        Entity entity = markers.remove(marker);
        if (entity != null) {
            entity.remove();
        }
    }

    public Entity spawnMarker(World world, Location at) {
        NPC npc = registry.createNPC(EntityType.ENDER_SIGNAL, "");
        npc.spawn(at);
        return npc.getEntity();
    }
}