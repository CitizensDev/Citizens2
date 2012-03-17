package net.citizensnpcs.npc;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.trait.TraitManager;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.Sheared;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.WoolColor;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.trait.waypoint.Waypoints;

public class CitizensTraitManager implements TraitManager {
    private final Map<String, Class<? extends Trait>> registered = new HashMap<String, Class<? extends Trait>>();
    private final Map<Class<? extends Trait>, Constructor<? extends Trait>> CACHED_CTORS = new HashMap<Class<? extends Trait>, Constructor<? extends Trait>>();

    public CitizensTraitManager() {
        // Register Citizens traits
        registerTrait(new TraitFactory(CurrentLocation.class).withName("location"));
        registerTrait(new TraitFactory(Equipment.class).withName("equipment"));
        registerTrait(new TraitFactory(Inventory.class).withName("inventory"));
        registerTrait(new TraitFactory(LookClose.class).withName("look-close"));
        registerTrait(new TraitFactory(MobType.class).withName("type"));
        registerTrait(new TraitFactory(Owner.class).withName("owner"));
        registerTrait(new TraitFactory(Powered.class).withName("powered"));
        registerTrait(new TraitFactory(Sheared.class).withName("sheared"));
        registerTrait(new TraitFactory(Spawned.class).withName("spawned"));
        registerTrait(new TraitFactory(Text.class).withName("text"));
        registerTrait(new TraitFactory(VillagerProfession.class).withName("profession"));
        registerTrait(new TraitFactory(Waypoints.class).withName("waypoints"));
        registerTrait(new TraitFactory(WoolColor.class).withName("wool-color"));
    }

    @Override
    public <T extends Trait> T getTrait(Class<T> clazz) {
        return getTrait(clazz, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Trait> T getTrait(String name) {
        if (!registered.containsKey(name))
            return null;
        return (T) create(registered.get(name), null);
    }

    @Override
    public void registerTrait(TraitFactory factory) {
        registered.put(factory.getName(), factory.getTraitClass());
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> T getTrait(Class<T> clazz, NPC npc) {
        for (Entry<String, Class<? extends Trait>> entry : registered.entrySet()) {
            if (!entry.getValue().equals(clazz))
                continue;
            Trait t = create(entry.getValue(), npc);
            t.setName(entry.getKey());
            return (T) t;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> T getTrait(String name, NPC npc) {
        // TODO: we could replace NPC with Object... and search for the
        // constructor
        Class<? extends Trait> clazz = registered.get(name);
        if (clazz == null)
            return null;
        Trait t = getTrait(clazz, npc);
        return (T) t;
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