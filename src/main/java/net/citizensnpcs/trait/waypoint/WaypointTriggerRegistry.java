package net.citizensnpcs.trait.waypoint;

import java.util.Map;

import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.util.DataKey;

import com.google.common.collect.Maps;

public class WaypointTriggerRegistry implements Persister {
    @Override
    public Object create(DataKey root) {
        String type = root.getString("type");
        Class<? extends WaypointTrigger> clazz = triggers.get(type);
        return clazz == null ? null : PersistenceLoader.load(clazz, root);
    }

    @Override
    public void save(Object instance, DataKey root) {
        PersistenceLoader.save(instance, root);
    }

    private static final Map<String, Class<? extends WaypointTrigger>> triggers = Maps.newHashMap();
    static {
        triggers.put("teleport", TeleportWaypointTrigger.class);
        triggers.put("delay", DelayWaypointTrigger.class);
    }
}
