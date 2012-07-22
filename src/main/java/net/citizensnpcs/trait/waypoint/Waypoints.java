package net.citizensnpcs.trait.waypoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;

import org.bukkit.entity.Player;

public class Waypoints extends Trait {
    private WaypointProvider provider = new LinearWaypointProvider();
    private String providerName = "linear";

    public Waypoints() {
        super("waypoints");
    }

    private WaypointProvider create(Class<? extends WaypointProvider> clazz) {
        if (!providers.containsKey(clazz))
            return null;
        try {
            return clazz.newInstance();
        } catch (Exception ex) {
            return null;
        }
    }

    public Editor getEditor(Player player) {
        return provider.createEditor(player);
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        provider = null;
        providerName = key.getString("provider", "linear");
        for (Entry<Class<? extends WaypointProvider>, String> entry : providers.entrySet()) {
            if (entry.getValue().equals(providerName)) {
                provider = create(entry.getKey());
                break;
            }
        }
        if (provider == null)
            return;
        provider.load(key.getRelative(providerName));
    }

    @Override
    public void onSpawn() {
        if (provider != null)
            provider.onSpawn(getNPC());
    }

    @Override
    public void save(DataKey key) {
        if (provider == null)
            return;
        provider.save(key.getRelative(providerName));
        key.setString("provider", providerName);
    }

    /**
     * Sets the current {@link WaypointProvider} by using the given class. The
     * class should have been registered using
     * {@link Waypoints#registerWaypointProvider(Class, String)}.
     * 
     * @param provider
     *            Class to set as waypoint provider
     */
    public void setWaypointProvider(Class<? extends WaypointProvider> clazz) {
        provider = create(clazz);
        if (provider != null) {
            providerName = providers.get(clazz);
        }
    }

    private static final Map<Class<? extends WaypointProvider>, String> providers = new HashMap<Class<? extends WaypointProvider>, String>();

    /**
     * Registers a {@link WaypointProvider}, which can be subsequently used by
     * NPCs.
     * 
     * @param clazz
     *            The class of the waypoint provider
     * @param name
     *            The name of the waypoint provider
     */
    public static void registerWaypointProvider(Class<? extends WaypointProvider> clazz, String name) {
        providers.put(clazz, name);
    }

    static {
        providers.put(LinearWaypointProvider.class, "linear");
    }
}