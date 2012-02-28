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
    private String providerName;
    private WaypointProvider provider = new LinearWaypointProvider();

    public Waypoints(NPC npc) {
        this.npc = npc;
        npc.getAI().registerNavigationCallback(provider.getCallback());
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

    public Editor getEditor(Player player) {
        return provider.createEditor(player);
    }

    public void setWaypointProvider(WaypointProvider provider, String name) {
        this.provider = provider;
        providerName = name;
    }

    public static void registerWaypointProvider(Class<? extends WaypointProvider> clazz, String name) {
        providers.register(clazz, name);
    }

    private static final InstanceFactory<WaypointProvider> providers = DefaultInstanceFactory.create();

    static {
        providers.register(LinearWaypointProvider.class, "linear");
    }
}