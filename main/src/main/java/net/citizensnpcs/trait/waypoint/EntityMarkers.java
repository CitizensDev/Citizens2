package net.citizensnpcs.trait.waypoint;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;

/**
 * A helper class for storing a number of entity markers. By default an entity marker is a non-persisted EnderSignal.
 */
public class EntityMarkers<T> {
    private final Map<T, Entity> markers = Maps.newHashMap();
    private final NPCRegistry registry = CitizensAPI.createCitizensBackedNPCRegistry(new MemoryNPCDataStore());
    private EntityType type;

    public EntityMarkers() {
        this(DEFAULT_ENTITY_TYPE);
    }

    public EntityMarkers(EntityType type) {
        this.type = type;

    }

    /**
     * Creates and persists (in memory) an {@link Entity} marker.
     *
     * @param marker
     *            the storage marker
     * @param at
     *            the spawn location
     * @return the created entity
     */
    public Entity createMarker(T marker, Location at) {
        Entity entity = spawnMarker(at.getWorld(), at);
        if (entity == null)
            return null;
        markers.put(marker, entity);
        return entity;
    }

    public void destroyMarkers() {
        registry.deregisterAll();
        markers.clear();
    }

    public void removeMarker(T marker) {
        Entity entity = markers.remove(marker);
        if (entity != null) {
            ((NPCHolder) entity).getNPC().destroy();
        }
    }

    /**
     * Spawns a marker {@link Entity} without storing it for later use.
     *
     * @param world
     *            the world (unused currently)
     * @param at
     *            the location
     * @return the spawned entity
     */
    public Entity spawnMarker(World world, Location at) {
        NPC npc = registry.createNPC(type, "");
        npc.data().set(NPC.Metadata.NAMEPLATE_VISIBLE, false);
        npc.spawn(at.clone().add(0.5, 0, 0.5), SpawnReason.CREATE);
        return npc.getEntity();
    }

    private static final EntityType DEFAULT_ENTITY_TYPE = Util.getFallbackEntityType("SHULKER_BULLET", "ENDER_SIGNAL");

}