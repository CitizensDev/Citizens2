package net.citizensnpcs.api.npc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public abstract class AbstractNPC implements NPC {
    private final GoalController goalController = new SimpleGoalController();
    private final int id;
    private final MetadataStore metadata = new SimpleMetadataStore() {
        @Override
        public void remove(String key) {
            super.remove(key);
            if (getEntity() != null) {
                getEntity().removeMetadata(key, CitizensAPI.getPlugin());
            }
        }

        @Override
        public void set(String key, Object data) {
            super.set(key, data);
            if (getEntity() != null) {
                getEntity().setMetadata(key, new FixedMetadataValue(CitizensAPI.getPlugin(), data));
            }
        }

        @Override
        public void setPersistent(String key, Object data) {
            super.setPersistent(key, data);
            if (getEntity() != null) {
                getEntity().setMetadata(key, new FixedMetadataValue(CitizensAPI.getPlugin(), data));
            }
        }
    };
    private String name;
    private final NPCRegistry registry;
    private final List<String> removedTraits = Lists.newArrayList();
    private final List<Runnable> runnables = Lists.newArrayList();
    private final SpeechController speechController = new SimpleSpeechController(this);
    protected final Map<Class<? extends Trait>, Trait> traits = Maps.newHashMap();
    private UUID uuid;

    protected AbstractNPC(UUID uuid, int id, String name, NPCRegistry registry) {
        if (name.length() > 16) {
            Messaging.severe("ID", id, "created with name length greater than 16, truncating", name, "to",
                    name.substring(0, 15));
            name = name.substring(0, 15);
        }
        this.uuid = uuid;
        this.id = id;
        this.registry = registry;
        this.name = name;
        CitizensAPI.getTraitFactory().addDefaultTraits(this);
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
        NPC copy = registry.createNPC(getTrait(MobType.class).getType(), getFullName());
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
        registry.deregister(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AbstractNPC other = (AbstractNPC) obj;
        if (id != other.id) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    @Deprecated
    public LivingEntity getBukkitEntity() {
        Entity entity = getEntity();
        if (entity == null || entity instanceof LivingEntity) {
            return (LivingEntity) entity;
        }
        throw new IllegalStateException("getBukkitEntity() called on a non-living NPC");
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
        if (!hasTrait(Speech.class)) {
            addTrait(Speech.class);
        }
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
    public NPCRegistry getOwningRegistry() {
        return registry;
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
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * (prime + id) + ((name == null) ? 0 : name.hashCode());
    }

    @Override
    public boolean hasTrait(Class<? extends Trait> trait) {
        return traits.containsKey(trait);
    }

    @Override
    public boolean isFlyable() {
        return data().get(NPC.FLYABLE_METADATA, false);
    }

    @Override
    public boolean isProtected() {
        return data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
    }

    @Override
    public boolean isSpawned() {
        return getEntity() != null && getEntity().isValid();
    }

    @Override
    public void load(final DataKey root) {
        metadata.loadFrom(root.getRelative("metadata"));
        if (root.keyExists("uuid")) {
            uuid = UUID.fromString(root.getString("uuid"));
        }

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
            } else {
                trait = CitizensAPI.getTraitFactory().getTrait(clazz);
                if (trait == null) {
                    Messaging.severeTr("citizens.notifications.trait-load-failed", traitKey.name(), getId());
                    continue;
                }
                addTrait(trait);
            }
            loadTrait(trait, traitKey);
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
        root.setString("uuid", uuid.toString());

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
        } else {
            root.setString("traitnames", "");
        }
        for (String name : removedTraits) {
            root.removeKey("traits." + name);
        }
        removedTraits.clear();
    }

    @Override
    public void setFlyable(boolean flyable) {
        data().setPersistent(NPC.FLYABLE_METADATA, flyable);
    }

    @Override
    public void setName(String name) {
        this.name = name;
        if (!isSpawned())
            return;
        Entity bukkitEntity = getEntity();
        if (bukkitEntity instanceof LivingEntity) {
            ((LivingEntity) bukkitEntity).setCustomName(getFullName());
        }
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

    private void teleport(final Entity entity, Location location, boolean loaded, int delay) {
        if (!loaded)
            location.getBlock().getChunk();
        final Entity passenger = entity.getPassenger();
        entity.eject();
        entity.teleport(location);
        if (passenger == null)
            return;
        teleport(passenger, location, true, delay++);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                entity.setPassenger(passenger);
            }
        };
        if (!location.getWorld().equals(entity.getWorld())) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), task, delay);
        } else {
            task.run();
        }
    }

    @Override
    public void teleport(Location location, TeleportCause cause) {
        if (!isSpawned())
            return;
        Entity entity = getEntity();
        while (entity.getVehicle() != null) {
            entity = entity.getVehicle();
        }
        teleport(entity, location, false, 5);
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
