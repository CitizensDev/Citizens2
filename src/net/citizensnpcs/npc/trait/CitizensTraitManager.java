package net.citizensnpcs.npc.trait;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.TraitManager;

public class CitizensTraitManager implements TraitManager {
    private final Map<String, Class<? extends Trait>> registered = new HashMap<String, Class<? extends Trait>>();
    private final Set<Trait> traits = new HashSet<Trait>();

    @Override
    public Trait getTrait(String name) {
        if (registered.get(name) == null) {
            return null;
        }
        for (Trait trait : traits) {
            if (trait.getName().equals(name)) {
                return trait;
            }
        }
        return null;
    }

    @Override
    public void registerTrait(Class<? extends Trait> trait) {
        if (registered.containsValue(trait)) {
            return;
        }
        try {
            Trait register = trait.newInstance();
            registered.put(register.getName(), trait);
            traits.add(register);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Collection<Trait> getRegisteredTraits() {
        return traits;
    }
}