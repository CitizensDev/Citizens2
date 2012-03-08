package net.citizensnpcs.trait.waypoint;

import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;

import org.bukkit.entity.Player;

public class Waypoints extends Trait {
    private final NPC npc;
    private WaypointProvider provider = new LinearWaypointProvider();
    private String providerName;

    public Waypoints(NPC npc) {
        this.npc = npc;
        npc.getAI().registerNavigationCallback(provider.getCallback());
    }

    public Editor getEditor(Player player) {
        return provider.createEditor(player);
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        providerName = key.getString("provider", "linear");
        for (Class<? extends WaypointProvider> clazz : providers.keySet())
            if (providers.get(clazz).equals(providerName)) {
                provider = create(clazz);
                break;
            }
        if (provider == null)
            return;
        provider.load(key.getRelative(providerName));
        npc.getAI().registerNavigationCallback(provider.getCallback());
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
        // TODO Probably needs to be changed/fixed. I attempted to make it work
        // with refactor. -aPunch
        provider = create(clazz);
        if (provider != null)
            providerName = providers.get(clazz);
    }

    private WaypointProvider create(Class<? extends WaypointProvider> clazz) {
        if (!providers.containsKey(clazz))
            return null;

        WaypointProvider provider;
        try {
            provider = clazz.newInstance();
        } catch (Exception ex) {
            provider = null;
        }

        return provider;
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