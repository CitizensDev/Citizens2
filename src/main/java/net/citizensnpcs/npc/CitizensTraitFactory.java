package net.citizensnpcs.npc;

import java.util.Map;
import java.util.Set;

import net.citizensnpcs.Metrics;
import net.citizensnpcs.Metrics.Graph;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.Behaviour;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.Saddle;
import net.citizensnpcs.trait.Sheared;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.WoolColor;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.trait.waypoint.Waypoints;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class CitizensTraitFactory implements TraitFactory {
    private final Map<String, Class<? extends Trait>> registered = Maps.newHashMap();

    public CitizensTraitFactory() {
        registerTrait(TraitInfo.create(Age.class).withName("age"));
        registerTrait(TraitInfo.create(CurrentLocation.class).withName("location"));
        registerTrait(TraitInfo.create(Equipment.class).withName("equipment"));
        registerTrait(TraitInfo.create(Inventory.class).withName("inventory"));
        registerTrait(TraitInfo.create(LookClose.class).withName("lookclose"));
        registerTrait(TraitInfo.create(MobType.class).withName("type"));
        registerTrait(TraitInfo.create(Owner.class).withName("owner"));
        registerTrait(TraitInfo.create(Powered.class).withName("powered"));
        registerTrait(TraitInfo.create(Saddle.class).withName("saddle"));
        registerTrait(TraitInfo.create(Sheared.class).withName("sheared"));
        registerTrait(TraitInfo.create(Spawned.class).withName("spawned"));
        registerTrait(TraitInfo.create(Text.class).withName("text"));
        registerTrait(TraitInfo.create(VillagerProfession.class).withName("profession"));
        registerTrait(TraitInfo.create(Waypoints.class).withName("waypoints"));
        registerTrait(TraitInfo.create(WoolColor.class).withName("woolcolor"));
        registerTrait(TraitInfo.create(Controllable.class).withName("controllable"));
        registerTrait(TraitInfo.create(Behaviour.class).withName("behaviour"));
        registerTrait(TraitInfo.create(Poses.class).withName("poses"));

        for (String trait : registered.keySet())
            INTERNAL_TRAITS.add(trait);
    }

    public void addPlotters(Graph graph) {
        for (Map.Entry<String, Class<? extends Trait>> entry : registered.entrySet()) {
            if (INTERNAL_TRAITS.contains(entry.getKey()))
                continue;
            final Class<? extends Trait> traitClass = entry.getValue();
            graph.addPlotter(new Metrics.Plotter(entry.getKey()) {
                @Override
                public int getValue() {
                    int numberUsingTrait = 0;
                    for (NPC npc : CitizensAPI.getNPCRegistry()) {
                        if (npc.hasTrait(traitClass))
                            ++numberUsingTrait;
                    }
                    return numberUsingTrait;
                }
            });
        }
    }

    private <T extends Trait> T create(Class<T> trait) {
        try {
            return trait.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public <T extends Trait> T getTrait(Class<T> clazz) {
        if (!registered.containsValue(clazz))
            return null;
        return create(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Trait> T getTrait(String name) {
        Class<? extends Trait> clazz = registered.get(name);
        if (clazz == null)
            return null;
        return (T) create(clazz);
    }

    @Override
    public Class<? extends Trait> getTraitClass(String name) {
        return registered.get(name.toLowerCase());
    }

    @Override
    public boolean isInternalTrait(Trait trait) {
        return INTERNAL_TRAITS.contains(trait.getName());
    }

    @Override
    public void registerTrait(TraitInfo info) {
        Preconditions.checkNotNull(info, "info cannot be null");
        if (registered.containsKey(info))
            throw new IllegalArgumentException("trait name already registered");
        registered.put(info.getTraitName(), info.getTraitClass());
    }

    private static final Set<String> INTERNAL_TRAITS = Sets.newHashSet();
}