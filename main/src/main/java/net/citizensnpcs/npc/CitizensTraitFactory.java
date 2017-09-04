package net.citizensnpcs.npc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
import net.citizensnpcs.api.trait.trait.Speech;
import net.citizensnpcs.trait.Age;
import net.citizensnpcs.trait.Anchors;
import net.citizensnpcs.trait.ArmorStandTrait;
import net.citizensnpcs.trait.BossBarTrait;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.MountTrait;
import net.citizensnpcs.trait.OcelotModifiers;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.RabbitType;
import net.citizensnpcs.trait.Saddle;
import net.citizensnpcs.trait.ScriptTrait;
import net.citizensnpcs.trait.SheepTrait;
import net.citizensnpcs.trait.ShulkerTrait;
import net.citizensnpcs.trait.SkinLayers;
import net.citizensnpcs.trait.SlimeSize;
import net.citizensnpcs.trait.VillagerProfession;
import net.citizensnpcs.trait.WitherTrait;
import net.citizensnpcs.trait.WolfModifiers;
import net.citizensnpcs.trait.WoolColor;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.trait.waypoint.Waypoints;

public class CitizensTraitFactory implements TraitFactory {
    private final List<TraitInfo> defaultTraits = Lists.newArrayList();
    private final Map<String, TraitInfo> registered = Maps.newHashMap();

    public CitizensTraitFactory() {
        registerTrait(TraitInfo.create(Age.class));
        registerTrait(TraitInfo.create(ArmorStandTrait.class));
        registerTrait(TraitInfo.create(Anchors.class));
        registerTrait(TraitInfo.create(BossBarTrait.class));
        registerTrait(TraitInfo.create(Controllable.class));
        registerTrait(TraitInfo.create(Equipment.class));
        registerTrait(TraitInfo.create(Gravity.class));
        registerTrait(TraitInfo.create(HorseModifiers.class));
        registerTrait(TraitInfo.create(Inventory.class));
        registerTrait(TraitInfo.create(CurrentLocation.class));
        registerTrait(TraitInfo.create(LookClose.class));
        registerTrait(TraitInfo.create(OcelotModifiers.class));
        registerTrait(TraitInfo.create(Owner.class));
        registerTrait(TraitInfo.create(Poses.class));
        registerTrait(TraitInfo.create(Powered.class));
        registerTrait(TraitInfo.create(RabbitType.class));
        registerTrait(TraitInfo.create(Saddle.class));
        registerTrait(TraitInfo.create(ScriptTrait.class));
        registerTrait(TraitInfo.create(SheepTrait.class));
        registerTrait(TraitInfo.create(ShulkerTrait.class));
        registerTrait(TraitInfo.create(SkinLayers.class));
        registerTrait(TraitInfo.create(MountTrait.class));
        registerTrait(TraitInfo.create(SlimeSize.class));
        registerTrait(TraitInfo.create(Spawned.class));
        registerTrait(TraitInfo.create(Speech.class));
        registerTrait(TraitInfo.create(Text.class));
        registerTrait(TraitInfo.create(MobType.class).asDefaultTrait());
        registerTrait(TraitInfo.create(Waypoints.class));
        registerTrait(TraitInfo.create(WitherTrait.class));
        registerTrait(TraitInfo.create(WoolColor.class));
        registerTrait(TraitInfo.create(WolfModifiers.class));
        registerTrait(TraitInfo.create(VillagerProfession.class));

        for (String trait : registered.keySet()) {
            INTERNAL_TRAITS.add(trait);
        }
    }

    @Override
    public void addDefaultTraits(NPC npc) {
        for (TraitInfo info : defaultTraits) {
            npc.addTrait(create(info));
        }
    }

    public void addPlotters(Graph graph) {
        for (Map.Entry<String, TraitInfo> entry : registered.entrySet()) {
            if (INTERNAL_TRAITS.contains(entry.getKey()))
                continue;
            final Class<? extends Trait> traitClass = entry.getValue().getTraitClass();
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

    private <T extends Trait> T create(TraitInfo info) {
        return info.tryCreateInstance();
    }

    @Override
    public void deregisterTrait(TraitInfo info) {
        Preconditions.checkNotNull(info, "info cannot be null");
        registered.remove(info.getTraitName());
    }

    @Override
    public <T extends Trait> T getTrait(Class<T> clazz) {
        for (TraitInfo entry : registered.values()) {
            if (clazz == entry.getTraitClass()) {
                return create(entry);
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Trait> T getTrait(String name) {
        TraitInfo info = registered.get(name.toLowerCase());
        if (info == null)
            return null;
        return (T) create(info);
    }

    @Override
    public Class<? extends Trait> getTraitClass(String name) {
        TraitInfo info = registered.get(name.toLowerCase());
        return info == null ? null : info.getTraitClass();
    }

    @Override
    public boolean isInternalTrait(Trait trait) {
        return INTERNAL_TRAITS.contains(trait.getName());
    }

    @Override
    public void registerTrait(TraitInfo info) {
        Preconditions.checkNotNull(info, "info cannot be null");
        if (registered.containsKey(info.getTraitName())) {
            System.out.println(info.getTraitClass());
            throw new IllegalArgumentException("trait name already registered");
        }
        registered.put(info.getTraitName(), info);
        if (info.isDefaultTrait()) {
            defaultTraits.add(info);
        }
    }

    private static final Set<String> INTERNAL_TRAITS = Sets.newHashSet();
}