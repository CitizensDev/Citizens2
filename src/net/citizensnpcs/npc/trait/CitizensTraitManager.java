package net.citizensnpcs.npc.trait;

import java.util.Map;

import net.citizensnpcs.api.Factory;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.TraitManager;

import com.google.common.collect.Maps;

public class CitizensTraitManager implements TraitManager {
    private final Map<String, Factory<? extends Trait>> registered = Maps.newHashMap();

    @Override
    public Trait getTrait(String name) {
        if (registered.get(name) == null)
            return null;
        return registered.get(name).create();
    }

    @Override
    public void registerTrait(String name, Class<? extends Trait> clazz) {
        registerTraitWithFactory(name, new ReflectionFactory(clazz));
    }

    @Override
    public void registerTraitWithFactory(String name, Factory<? extends Trait> factory) {
        if (registered.get(name) != null)
            throw new IllegalArgumentException("Trait factory already registered.");
        registered.put(name, factory);
    }

    private static class ReflectionFactory implements Factory<Trait> {
        private final Class<? extends Trait> clazz;

        private ReflectionFactory(Class<? extends Trait> clazz) {
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