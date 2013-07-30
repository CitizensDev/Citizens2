package net.citizensnpcs.api.npc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.ai.SimpleGoalController;
import net.citizensnpcs.api.ai.speech.SimpleSpeechController;
import net.citizensnpcs.api.ai.speech.SpeechController;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCAddTraitEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRemoveTraitEvent;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Speech;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.api.util.Messaging;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public abstract class AbstractNPC implements NPC {
    private final GoalController goalController = new SimpleGoalController();
    private final int id;
    protected final MetadataStore metadata = new SimpleMetadataStore(this);
    private String name;
    private final List<String> removedTraits = Lists.newArrayList();
    protected final List<Runnable> runnables = Lists.newArrayList();
    private final SpeechController speechController = new SimpleSpeechController(this);
    protected final Map<Class<? extends Trait>, Trait> traits = Maps.newHashMap();

    protected AbstractNPC(int id, String name) {
        if (name.length() > 16) {
            Messaging.severe("ID", id, "created with name length greater than 16, truncating", name, "to",
                    name.substring(0, 15));
            name = name.substring(0, 15);
        }
        this.id = id;
        this.name = name;
        addTrait(MobType.class);
    }

    @Override
    public void addTrait(Class<? extends Trait> clazz) {
        addTrait(getTraitFor(clazz));
    }

    @Override
    public void addTrait(Trait trait) {
        if (trait == null) {
            Messaging.severe("Cannot register a null trait. Was it registered properly?");
            return;
        }

        if (trait.getNPC() == null) {
            trait.linkToNPC(this);
        }

        // if an existing trait is being replaced, we need to remove the
        // currently registered runnable to avoid conflicts
        Trait replaced = traits.get(trait.getClass());

        Bukkit.getPluginManager().registerEvents(trait, CitizensAPI.getPlugin());
        traits.put(trait.getClass(), trait);
        if (isSpawned())
            trait.onSpawn();

        if (trait.isRunImplemented()) {
            if (replaced != null)
                runnables.remove(replaced);
            runnables.add(trait);
        }

        Bukkit.getPluginManager().callEvent(new NPCAddTraitEvent(this, trait));
    }

    @Override
    public NPC clone() {
        NPC copy = CitizensAPI.getNPCRegistry().createNPC(getTrait(MobType.class).getType(), getFullName());
        DataKey key = new MemoryDataKey();
        this.save(key);
        copy.load(key);

        for (Trait trait : copy.getTraits()) {
            trait.onCopy();
        }
        return copy;
    }

    @Override
    public MetadataStore data() {
        return this.metadata;
    }

    @Override
    public boolean despawn() {
        return despawn(DespawnReason.PLUGIN);
    }

    @Override
    public void destroy() {
        Bukkit.getPluginManager().callEvent(new NPCRemoveEvent(this));
        runnables.clear();
        for (Trait trait : traits.values()) {
            HandlerList.unregisterAll(trait);
            trait.onRemove();
        }
        traits.clear();
        CitizensAPI.getNPCRegistry().deregister(this);
    }

    @Override
    public GoalController getDefaultGoalController() {
        return goalController;
    }

    @Override
    public SpeechController getDefaultSpeechController() {
        // TODO: Remove in future versions.
        // This is here to add the Speech trait to any existing NPCs
        // that were created pre-SpeechController, if invoked.
        if (!hasTrait(Speech.class))
            addTrait(Speech.class);
        return speechController;
    }

    @Override
    public String getFullName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        String parsed = name;
        for (ChatColor color : ChatColor.values())
            if (parsed.contains("<" + color.getChar() + ">"))
                parsed = parsed.replace("<" + color.getChar() + ">", "");
        return parsed;
    }

    @Override
    public <T extends Trait> T getTrait(Class<T> clazz) {
        Trait trait = traits.get(clazz);
        if (trait == null) {
            trait = getTraitFor(clazz);
            addTrait(trait);
        }
        return trait != null ? clazz.cast(trait) : null;
    }

    protected Trait getTraitFor(Class<? extends Trait> clazz) {
        return CitizensAPI.getTraitFactory().getTrait(clazz);
    }

    @Override
    public Iterable<Trait> getTraits() {
        return traits.values();
    }

    @Override
    public boolean hasTrait(Class<? extends Trait> trait) {
        return traits.containsKey(trait);
    }

    @Override
    public boolean isProtected() {
        return data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
    }

    @Override
    public void load(final DataKey root) {
        metadata.loadFrom(root.getRelative("metadata"));
        // Load traits

        String traitNames = root.getString("traitnames");
        Set<DataKey> keys = Sets.newHashSet(root.getRelative("traits").getSubKeys());
        Iterables.addAll(keys, Iterables.transform(Splitter.on(',').split(traitNames), new Function<String, DataKey>() {
            @Override
            public DataKey apply(@Nullable String input) {
                return root.getRelative("traits." + input);
            }
        }));
        for (DataKey traitKey : keys) {
            if (traitKey.keyExists("enabled") && !traitKey.getBoolean("enabled")
                    && traitKey.getRaw("enabled") instanceof Boolean) {
                // avoid YAML coercing map existence to boolean
                continue;
            }
            Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(traitKey.name());
            Trait trait;
            if (hasTrait(clazz)) {
                trait = getTrait(clazz);
                loadTrait(trait, traitKey);
            } else {
                trait = CitizensAPI.getTraitFactory().getTrait(clazz);
                if (trait == null) {
                    Messaging.severeTr("citizens.notifications.trait-load-failed", traitKey.name(), getId());
                    continue;
                }
                loadTrait(trait, traitKey);
                addTrait(trait);
            }
        }
    }

    private void loadTrait(Trait trait, DataKey traitKey) {
        try {
            trait.load(traitKey);
            PersistenceLoader.load(trait, traitKey);
        } catch (Throwable ex) {
            Messaging.logTr("citizens.notifications.trait-load-failed", traitKey.name(), getId());
        }
    }

    @Override
    public void removeTrait(Class<? extends Trait> traitClass) {
        Trait trait = traits.remove(traitClass);
        if (trait != null) {
            Bukkit.getPluginManager().callEvent(new NPCRemoveTraitEvent(this, trait));
            removedTraits.add(trait.getName());
            if (trait.isRunImplemented())
                runnables.remove(trait);
            HandlerList.unregisterAll(trait);
            trait.onRemove();
        }
    }

    @Override
    public void save(DataKey root) {
        metadata.saveTo(root.getRelative("metadata"));
        root.setString("name", getFullName());

        // Save all existing traits
        StringBuilder traitNames = new StringBuilder();
        for (Trait trait : traits.values()) {
            DataKey traitKey = root.getRelative("traits." + trait.getName());
            trait.save(traitKey);
            PersistenceLoader.save(trait, traitKey);
            removedTraits.remove(trait.getName());
            traitNames.append(trait.getName() + ",");
        }
        if (traitNames.length() > 0) {
            root.setString("traitnames", traitNames.substring(0, traitNames.length() - 1));
        } else
            root.setString("traitnames", "");
        for (String name : removedTraits) {
            root.removeKey("traits." + name);
        }
        removedTraits.clear();
    }

    @Override
    public void setName(String name) {
        this.name = name;
        if (!isSpawned())
            return;
        LivingEntity bukkitEntity = getBukkitEntity();
        bukkitEntity.setCustomName(getFullName());
        if (bukkitEntity.getType() == EntityType.PLAYER) {
            Location old = bukkitEntity.getLocation();
            despawn(DespawnReason.PENDING_RESPAWN);
            spawn(old);
        }
    }

    @Override
    public void setProtected(boolean isProtected) {
        data().setPersistent(NPC.DEFAULT_PROTECTED_METADATA, isProtected);
    }

    public void update() {
        for (int i = 0; i < runnables.size(); ++i) {
            runnables.get(i).run();
        }
        if (isSpawned()) {
            goalController.run();
        }
    }
}