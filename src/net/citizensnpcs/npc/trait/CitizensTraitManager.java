package net.citizensnpcs.npc.trait;

import java.util.HashMap;
import java.util.Map;

import net.citizensnpcs.api.Factory;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.TraitManager;

public class CitizensTraitManager implements TraitManager {
    private final Map<String, Factory<? extends Trait>> registered = new HashMap<String, Factory<? extends Trait>>();

    @Override
    public Trait getTrait(String name) {
        if (registered.get(name) == null)
            return null;
        return registered.get(name).create();
    }

    @Override
    public void registerTrait(String name, Class<? extends Trait> clazz) {
        registerTraitWithFactory(name, new DefaultTraitFactory(clazz));
    }

    @Override
    public void registerTraitWithFactory(String name, Factory<? extends Trait> factory) {
        if (registered.get(name) != null)
            throw new IllegalArgumentException("A trait factory for the trait '" + name
                    + "' has already been registered.");
        registered.put(name, factory);
    }

    private static class DefaultTraitFactory implements Factory<Trait> {
        private final Class<? extends Trait> clazz;

        private DefaultTraitFactory(Class<? extends Trait> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Trait create() {
            try {
                return clazz.newInstance();
            } catch (Exception ex) {
                return null;
            }
        }
    }
}