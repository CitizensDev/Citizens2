package net.citizensnpcs.npc;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.api.exception.TraitException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.trait.TraitManager;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.api.trait.trait.SpawnLocation;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.trait.waypoint.Waypoints;

public class CitizensTraitManager implements TraitManager {
    private final Map<String, Class<? extends Trait>> registered = new HashMap<String, Class<? extends Trait>>();

    public CitizensTraitManager() {
        // Register Citizens traits
        registerTrait(new TraitFactory(Equipment.class).withName("equipment"));
        registerTrait(new TraitFactory(Inventory.class).withName("inventory"));
        registerTrait(new TraitFactory(LookClose.class).withName("look-close"));
        registerTrait(new TraitFactory(MobType.class).withName("type"));
        registerTrait(new TraitFactory(Owner.class).withName("owner"));
        registerTrait(new TraitFactory(Powered.class).withName("powered"));
        registerTrait(new TraitFactory(Spawned.class).withName("spawned"));
        registerTrait(new TraitFactory(SpawnLocation.class).withName("location"));
        registerTrait(new TraitFactory(Text.class).withName("text"));
        registerTrait(new TraitFactory(Waypoints.class).withName("waypoints"));
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
        for (String name : registered.keySet())
            if (registered.get(name).equals(clazz)) {
                Trait t = create(registered.get(name), npc);
                try {
                    if (t.getName() == null)
                        t.setName(name);
                    return (T) t;
                } catch (TraitException ex) {
                    ex.printStackTrace();
                }
            }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> T getTrait(String name, NPC npc) {
        if (!registered.containsKey(name))
            return null;
        Trait t = getTrait(registered.get(name), npc);
        try {
            if (t.getName() == null)
                t.setName(name);
            return (T) t;
        } catch (TraitException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T extends Trait> T create(Class<T> trait, NPC npc) {
        Constructor<? extends Trait> constructor;

        try {
            constructor = trait.getConstructor(NPC.class);
        } catch (Exception ex) {
            constructor = null;
        }

        try {
            if (npc == null)
                return (T) trait.newInstance();
            return constructor != null ? (T) constructor.newInstance(npc) : (T) trait.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}