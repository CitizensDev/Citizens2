package net.citizensnpcs.npc;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
import net.citizensnpcs.trait.ClickRedirectTrait;
import net.citizensnpcs.trait.CommandTrait;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.trait.DropsTrait;
import net.citizensnpcs.trait.EnderCrystalTrait;
import net.citizensnpcs.trait.EndermanTrait;
import net.citizensnpcs.trait.FollowTrait;
import net.citizensnpcs.trait.GameModeTrait;
import net.citizensnpcs.trait.Gravity;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.HorseModifiers;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.MountTrait;
import net.citizensnpcs.trait.OcelotModifiers;
import net.citizensnpcs.trait.Poses;
import net.citizensnpcs.trait.Powered;
import net.citizensnpcs.trait.RabbitType;
import net.citizensnpcs.trait.Saddle;
import net.citizensnpcs.trait.ScoreboardTrait;
import net.citizensnpcs.trait.ScriptTrait;
import net.citizensnpcs.trait.SheepTrait;
import net.citizensnpcs.trait.SkinLayers;
import net.citizensnpcs.trait.SkinTrait;
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
        registerTrait(TraitInfo.create(ClickRedirectTrait.class));
        registerTrait(TraitInfo.create(CommandTrait.class));
        registerTrait(TraitInfo.create(Controllable.class));
        registerTrait(TraitInfo.create(CurrentLocation.class));
        registerTrait(TraitInfo.create(DropsTrait.class));
        registerTrait(TraitInfo.create(EnderCrystalTrait.class));
        registerTrait(TraitInfo.create(EndermanTrait.class));
        registerTrait(TraitInfo.create(Equipment.class));
        registerTrait(TraitInfo.create(FollowTrait.class));
        registerTrait(TraitInfo.create(GameModeTrait.class));
        registerTrait(TraitInfo.create(Gravity.class));
        registerTrait(TraitInfo.create(HorseModifiers.class));
        registerTrait(TraitInfo.create(HologramTrait.class));
        registerTrait(TraitInfo.create(Inventory.class));
        registerTrait(TraitInfo.create(LookClose.class));
        registerTrait(TraitInfo.create(OcelotModifiers.class));
        registerTrait(TraitInfo.create(Owner.class));
        registerTrait(TraitInfo.create(Poses.class));
        registerTrait(TraitInfo.create(Powered.class));
        registerTrait(TraitInfo.create(RabbitType.class));
        registerTrait(TraitInfo.create(Saddle.class));
        registerTrait(TraitInfo.create(ScoreboardTrait.class));
        registerTrait(TraitInfo.create(ScriptTrait.class));
        registerTrait(TraitInfo.create(SheepTrait.class));
        registerTrait(TraitInfo.create(SkinLayers.class));
        registerTrait(TraitInfo.create(SkinTrait.class));
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

    private <T extends Trait> T create(TraitInfo info) {
        return info.tryCreateInstance();
    }

    @Override
    public void deregisterTrait(TraitInfo info) {
        Preconditions.checkNotNull(info, "info cannot be null");
        registered.remove(info.getTraitName());
    }

    @Override
    public Collection<TraitInfo> getRegisteredTraits() {
        return registered.values();
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

    public Map<String, Integer> getTraitPlot() {
        Map<String, Integer> counts = Maps.newHashMap();
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            for (Trait trait : npc.getTraits()) {
                if (INTERNAL_TRAITS.contains(trait.getName()))
                    continue;
                counts.put(trait.getName(), counts.getOrDefault(trait.getName(), 0) + 1);
            }
        }
        return counts;
    }

    @Override
    public boolean isInternalTrait(Trait trait) {
        return INTERNAL_TRAITS.contains(trait.getName());
    }

    @Override
    public void registerTrait(TraitInfo info) {
        Preconditions.checkNotNull(info, "info cannot be null");
        if (registered.containsKey(info.getTraitName())) {
            throw new IllegalArgumentException("Trait name " + info.getTraitName() + " already registered");
        }
        registered.put(info.getTraitName(), info);
        if (info.isDefaultTrait()) {
            defaultTraits.add(info);
        }
    }

    private static final Set<String> INTERNAL_TRAITS = Sets.newHashSet();
}