package net.citizensnpcs.trait.waypoint;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.DefaultInstanceFactory;
import net.citizensnpcs.api.trait.InstanceFactory;
import net.citizensnpcs.api.trait.SaveId;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.editor.Editor;

import org.bukkit.entity.Player;

@SaveId("waypoints")
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
        provider = providers.getInstance(providerName);
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
     * Sets the current {@link WaypointProvider} by using the given name. The
     * name should have been registered using
     * {@link Waypoints#registerWaypointProvider(Class, String)}.
     * 
     * @param provider
     * @param name
     */
    public void setWaypointProvider(String name) {
        this.provider = providers.getInstance(name);
        if (this.provider != null) {
            providerName = name;
        }
    }

    private static final InstanceFactory<WaypointProvider> providers = DefaultInstanceFactory.create();

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
        providers.register(clazz, name);
    }

    static {
        providers.register(LinearWaypointProvider.class, "linear");
    }
}