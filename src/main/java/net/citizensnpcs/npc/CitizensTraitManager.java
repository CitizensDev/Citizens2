package net.citizensnpcs.npc;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.plugin.Plugin;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.trait.TraitManager;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.Saddle;
import net.citizensnpcs.trait.Sheared;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.WoolColor;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.trait.waypoint.Waypoints;

public class CitizensTraitManager implements TraitManager {
    private final Map<Plugin, Map<String, Class<? extends Trait>>> registered = new HashMap<Plugin, Map<String, Class<? extends Trait>>>();
    private final Map<Class<? extends Trait>, Constructor<? extends Trait>> CACHED_CTORS = new HashMap<Class<? extends Trait>, Constructor<? extends Trait>>();

    public CitizensTraitManager(Citizens plugin) {
        // Register Citizens traits
        registerTrait(new TraitFactory(Age.class).withName("age").withPlugin(plugin));
        registerTrait(new TraitFactory(CurrentLocation.class).withName("location").withPlugin(plugin));
        registerTrait(new TraitFactory(Equipment.class).withName("equipment").withPlugin(plugin));
        registerTrait(new TraitFactory(Inventory.class).withName("inventory").withPlugin(plugin));
        registerTrait(new TraitFactory(LookClose.class).withName("look-close").withPlugin(plugin));
        registerTrait(new TraitFactory(MobType.class).withName("type").withPlugin(plugin));
        registerTrait(new TraitFactory(Owner.class).withName("owner").withPlugin(plugin));
        registerTrait(new TraitFactory(Powered.class).withName("powered").withPlugin(plugin));
        registerTrait(new TraitFactory(Saddle.class).withName("saddle").withPlugin(plugin));
        registerTrait(new TraitFactory(Sheared.class).withName("sheared").withPlugin(plugin));
        registerTrait(new TraitFactory(Spawned.class).withName("spawned").withPlugin(plugin));
        registerTrait(new TraitFactory(Text.class).withName("text").withPlugin(plugin));
        registerTrait(new TraitFactory(VillagerProfession.class).withName("profession").withPlugin(plugin));
        registerTrait(new TraitFactory(Waypoints.class).withName("waypoints").withPlugin(plugin));
        registerTrait(new TraitFactory(WoolColor.class).withName("wool-color").withPlugin(plugin));
    }

    @Override
    public <T extends Trait> T getTrait(Class<T> clazz) {
        return getTrait(clazz, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Trait> T getTrait(String name) {
        for (Plugin plugin : registered.keySet()) {
            if (!registered.get(plugin).containsKey(name))
                return null;
            return (T) create(registered.get(plugin).get(name), null);
        }
        return null;
    }

    @Override
    public void registerTrait(TraitFactory factory) {
        Map<String, Class<? extends Trait>> map = registered.get(factory.getTraitPlugin());
        if (map == null)
            map = new HashMap<String, Class<? extends Trait>>();
        map.put(factory.getTraitName(), factory.getTraitClass());
        registered.put(factory.getTraitPlugin(), map);
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> T getTrait(Class<T> clazz, NPC npc) {
        for (Entry<Plugin, Map<String, Class<? extends Trait>>> entry : registered.entrySet()) {
            for (Entry<String, Class<? extends Trait>> subEntry : entry.getValue().entrySet()) {
                if (!subEntry.getValue().equals(clazz))
                    continue;
                Trait trait = create(subEntry.getValue(), npc);
                trait.setName(subEntry.getKey());
                trait.setPlugin(entry.getKey());
                return (T) trait;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> T getTrait(String name, NPC npc) {
        for (Plugin plugin : registered.keySet()) {
            Class<? extends Trait> clazz = registered.get(plugin).get(name);
            if (clazz == null)
                continue;
            return (T) getTrait(clazz, npc);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends Trait> T create(Class<T> trait, NPC npc) {
        Constructor<? extends Trait> constructor;

        if (!CACHED_CTORS.containsKey(trait)) {
            try {
                constructor = trait.getConstructor(NPC.class);
                constructor.setAccessible(true);
            } catch (Exception ex) {
                constructor = null;
            }
            CACHED_CTORS.put(trait, constructor);
        } else
            constructor = CACHED_CTORS.get(trait);

        try {
            if (constructor == null || npc == null)
                return trait.newInstance();
            return (T) constructor.newInstance(npc);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}