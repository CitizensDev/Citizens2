package net.citizensnpcs.trait.waypoint;

import java.util.Map;
import java.util.Map.Entry;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

public class Waypoints extends Trait {
    private WaypointProvider provider = new LinearWaypointProvider();
    private String providerName = "linear";

    public Waypoints() {
        super("waypoints");
    }

    private WaypointProvider create(Class<? extends WaypointProvider> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void describeProviders(CommandSender sender) {
        Messaging.sendTr(sender, Messages.AVAILABLE_WAYPOINT_PROVIDERS);
        for (String name : providers.keySet()) {
            Messaging.send(sender, "    - " + StringHelper.wrap(name));
        }
    }

    /**
     * Returns the current {@link WaypointProvider}. May be null during
     * initialisation.
     * 
     * @return The current provider
     */
    public WaypointProvider getCurrentProvider() {
        return provider;
    }

    /**
     * @return The current provider name
     */
    public String getCurrentProviderName() {
        return providerName;
    }

    public Editor getEditor(Player player) {
        return provider.createEditor(player);
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        provider = null;
        providerName = key.getString("provider", "linear");
        for (Entry<String, Class<? extends WaypointProvider>> entry : providers.entrySet()) {
            if (entry.getKey().equals(providerName)) {
                provider = create(entry.getValue());
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
     * Sets the current {@link WaypointProvider} using the given name.
     * 
     * @param name
     *            The name of the waypoint provider, registered using
     *            {@link #registerWaypointProvider(Class, String)}
     * @return Whether the operation succeeded
     */
    public boolean setWaypointProvider(String name) {
        name = name.toLowerCase();
        Class<? extends WaypointProvider> clazz = providers.get(name);
        if (clazz == null)
            return false;
        provider = create(clazz);
        if (provider == null)
            return false;
        providerName = name;
        if (npc != null && npc.isSpawned())
            provider.onSpawn(npc);
        return true;
    }

    private static final Map<String, Class<? extends WaypointProvider>> providers = Maps.newHashMap();

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
        providers.put(name, clazz);
    }

    static {
        providers.put("linear", LinearWaypointProvider.class);
        providers.put("wander", WanderWaypointProvider.class);
    }
}