package net.citizensnpcs.api.npc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.GoalController;
import net.citizensnpcs.api.ai.SimpleGoalController;
import net.citizensnpcs.api.ai.speech.SimpleSpeechController;
import net.citizensnpcs.api.ai.speech.SpeechController;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCAddTraitEvent;
import net.citizensnpcs.api.event.NPCCloneEvent;
import net.citizensnpcs.api.event.NPCRemoveByCommandSenderEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRemoveTraitEvent;
import net.citizensnpcs.api.event.NPCRenameEvent;
import net.citizensnpcs.api.event.NPCTeleportEvent;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.PlayerFilter;
import net.citizensnpcs.api.trait.trait.Speech;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;
import net.citizensnpcs.api.util.MemoryDataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.api.util.SpigotUtil;

public abstract class AbstractNPC implements NPC {
    protected Object coloredNameComponentCache;
    protected String coloredNameStringCache;
    private final GoalController goalController = new SimpleGoalController();
    private final int id;
    private Supplier<ItemStack> itemProvider = () -> {
        Material id = Material.STONE;
        int data = data().get(NPC.Metadata.ITEM_DATA, data().get("falling-block-data", 0));
        if (data().has(NPC.Metadata.ITEM_ID)) {
            id = Material.getMaterial(data().<String> get(NPC.Metadata.ITEM_ID), false);
        }
        if (id == Material.AIR) {
            id = Material.STONE;
            Messaging.severe(getId(), "invalid Material: converted to stone");
        }
        return new org.bukkit.inventory.ItemStack(id, data().get(NPC.Metadata.ITEM_AMOUNT, 1), (short) data);
    };
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
    private final UUID uuid;

    protected AbstractNPC(UUID uuid, int id, String name, NPCRegistry registry) {
        this.uuid = uuid;
        this.id = id;
        this.registry = registry;
        setNameInternal(name);
        CitizensAPI.getTraitFactory().addDefaultTraits(this);
    }

    @Override
    public void addRunnable(Runnable runnable) {
        this.runnables.add(runnable);
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
        Class<? extends Trait> clazz = trait.getClass();
        Trait replaced = traits.get(clazz);

        Bukkit.getPluginManager().registerEvents(trait, CitizensAPI.getPlugin());
        traits.put(clazz, trait);
        if (isSpawned()) {
            trait.onSpawn();
        }

        if (trait.isRunImplemented()) {
            if (replaced != null) {
                runnables.remove(replaced);
            }
            runnables.add(trait);
        }

        Bukkit.getPluginManager().callEvent(new NPCAddTraitEvent(this, trait));
    }

    @Override
    public NPC clone() {
        return copy();
    }

    @Override
    public NPC copy() {
        NPC copy = registry.createNPC(getOrAddTrait(MobType.class).getType(), getRawName());
        DataKey key = new MemoryDataKey();
        save(key);
        copy.load(key);

        for (Trait trait : copy.getTraits()) {
            trait.onCopy();
        }
        Bukkit.getPluginManager().callEvent(new NPCCloneEvent(this, copy));
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
        goalController.clear();
        registry.deregister(this);
    }

    @Override
    public void destroy(CommandSender source) {
        Bukkit.getPluginManager().callEvent(new NPCRemoveByCommandSenderEvent(this, source));
        destroy();
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
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
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

    protected EntityType getEntityType() {
        return isSpawned() ? getEntity().getType() : getOrAddTrait(MobType.class).getType();
    }

    @Override
    public String getFullName() {
        int nameLength = SpigotUtil.getMaxNameLength(getEntityType());
        String replaced = Placeholders.replace(
                coloredNameStringCache != null ? coloredNameStringCache : Messaging.parseComponents(name), null, this);
        if (replaced.length() > nameLength) {
            Messaging.severe("ID", id, "created with name length greater than " + nameLength + ", truncating", replaced,
                    "to", replaced.substring(0, nameLength));
            replaced = replaced.substring(0, nameLength);
        }
        return replaced;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Supplier<ItemStack> getItemProvider() {
        return itemProvider;
    }

    @Override
    public String getName() {
        return ChatColor.stripColor(coloredNameStringCache);
    }

    @Override
    public <T extends Trait> T getOrAddTrait(Class<T> clazz) {
        Trait trait = traits.get(clazz);
        if (trait == null) {
            trait = getTraitFor(clazz);
            addTrait(trait);
        }
        return clazz.cast(trait);
    }

    @Override
    public NPCRegistry getOwningRegistry() {
        return registry;
    }

    @Override
    public String getRawName() {
        return name;
    }

    @Override
    @Deprecated
    public <T extends Trait> T getTrait(Class<T> clazz) {
        return getOrAddTrait(clazz);
    }

    protected Trait getTraitFor(Class<? extends Trait> clazz) {
        return CitizensAPI.getTraitFactory().getTrait(clazz);
    }

    @Override
    public <T extends Trait> T getTraitNullable(Class<T> clazz) {
        return clazz.cast(traits.get(clazz));
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
        return 31 + uuid.hashCode();
    }

    @Override
    public boolean hasTrait(Class<? extends Trait> trait) {
        return traits.containsKey(trait);
    }

    @Override
    public boolean isFlyable() {
        return data().get(NPC.Metadata.FLYABLE, false);
    }

    @Override
    public boolean isHiddenFrom(Player player) {
        PlayerFilter filter = getTraitNullable(PlayerFilter.class);
        return filter != null ? filter.isHidden(player) : false;
    }

    @Override
    public boolean isProtected() {
        return data().get(NPC.Metadata.DEFAULT_PROTECTED, true);
    }

    @Override
    public boolean isPushableByFluids() {
        return data().get(NPC.Metadata.FLUID_PUSHABLE, isProtected());
    }

    @Override
    public void load(final DataKey root) {
        setNameInternal(root.getString("name"));
        if (root.keyExists("itemprovider")) {
            ItemStack item = ItemStorage.loadItemStack(root.getRelative("itemprovider"));
            itemProvider = () -> item;
        }
        metadata.loadFrom(root.getRelative("metadata"));

        String traitNames = root.getString("traitnames");
        Set<DataKey> keys = Sets.newHashSet(root.getRelative("traits").getSubKeys());
        Iterables.addAll(keys,
                Iterables.transform(Splitter.on(',').split(traitNames), input -> root.getRelative("traits." + input)));
        DataKey locationKey = root.getRelative("traits.location");
        if (locationKey.keyExists()) {
            loadTraitFromKey(locationKey);
            keys.remove(locationKey);
        }
        for (DataKey key : keys) {
            loadTraitFromKey(key);
        }
    }

    private void loadTraitFromKey(DataKey traitKey) {
        if (traitKey.name().equals("smoothrotationtrait")) {
            traitKey.removeKey("");
            return;
        }
        Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(traitKey.name());
        Trait trait;
        if (hasTrait(clazz)) {
            trait = getTraitNullable(clazz);
        } else {
            trait = CitizensAPI.getTraitFactory().getTrait(clazz);
            if (trait == null) {
                Messaging.severeTr("citizens.notifications.trait-load-failed", traitKey.name(), getId());
                return;
            }
            addTrait(trait);
        }
        try {
            PersistenceLoader.load(trait, traitKey);
            trait.load(traitKey);
        } catch (Throwable ex) {
            if (Messaging.isDebugging()) {
                ex.printStackTrace();
            }
            Messaging.logTr("citizens.notifications.trait-load-failed", traitKey.name(), getId());
        }
    }

    @Override
    public void removeTrait(Class<? extends Trait> traitClass) {
        Trait trait = traits.remove(traitClass);
        if (trait != null) {
            Bukkit.getPluginManager().callEvent(new NPCRemoveTraitEvent(this, trait));
            removedTraits.add(trait.getName());
            if (trait.isRunImplemented()) {
                runnables.remove(trait);
            }
            HandlerList.unregisterAll(trait);
            trait.onRemove();
        }
    }

    @Override
    public boolean requiresNameHologram() {
        return getEntityType() != EntityType.ARMOR_STAND
                && ((name.length() > 16 && getEntityType() == EntityType.PLAYER)
                        || data().get(NPC.Metadata.ALWAYS_USE_NAME_HOLOGRAM, false)
                        || (coloredNameStringCache != null && coloredNameStringCache.contains("§x"))
                        || !Placeholders.replace(name, null, this).equals(name));
    }

    @Override
    public void save(DataKey root) {
        if (!metadata.get(NPC.Metadata.SHOULD_SAVE, true))
            return;

        metadata.saveTo(root.getRelative("metadata"));
        root.setString("name", name);
        root.setString("uuid", uuid.toString());

        if (data().has(NPC.Metadata.ITEM_ID)) {
            ItemStack stack = itemProvider.get();
            ItemStorage.saveItem(root.getRelative("itemprovider"), stack);
        } else {
            root.removeKey("itemprovider");
        }

        // Save all existing traits
        StringBuilder traitNames = new StringBuilder();
        for (Trait trait : traits.values()) {
            DataKey traitKey = root.getRelative("traits." + trait.getName());
            trait.save(traitKey);
            try {
                PersistenceLoader.save(trait, traitKey);
            } catch (Throwable t) {
                Messaging.log("PersistenceLoader failed for", trait);
                t.printStackTrace();
                continue;
            }
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
    public void setAlwaysUseNameHologram(boolean use) {
        data().setPersistent(NPC.Metadata.ALWAYS_USE_NAME_HOLOGRAM, use);
    }

    @Override
    public void setFlyable(boolean flyable) {
        data().setPersistent(NPC.Metadata.FLYABLE, flyable);
    }

    @Override
    public void setItemProvider(Supplier<ItemStack> provider) {
        this.itemProvider = provider;
        ItemStack stack = provider.get();
        if (stack != null) {
            data().set(NPC.Metadata.ITEM_ID, stack.getType().name());
            data().set(NPC.Metadata.ITEM_DATA, stack.getData().getData());
            data().set(NPC.Metadata.ITEM_AMOUNT, stack.getAmount());
        }
    }

    @Override
    public void setName(String name) {
        if (name.equals(this.name))
            return;

        NPCRenameEvent event = new NPCRenameEvent(this, this.name, name);
        Bukkit.getPluginManager().callEvent(event);
        setNameInternal(event.getNewName());

        if (!isSpawned())
            return;

        Entity bukkitEntity = getEntity();
        updateCustomName();

        if (bukkitEntity.getType() == EntityType.PLAYER && !requiresNameHologram()) {
            Location old = bukkitEntity.getLocation();
            despawn(DespawnReason.PENDING_RESPAWN);
            spawn(old);
        }
    }

    private void setNameInternal(String name) {
        this.name = name;
        coloredNameComponentCache = Messaging.minecraftComponentFromRawMessage(this.name);
        coloredNameStringCache = Messaging.parseComponents(this.name);
    }

    @Override
    public void setProtected(boolean isProtected) {
        data().setPersistent(NPC.Metadata.DEFAULT_PROTECTED, isProtected);
    }

    @Override
    public void setUseMinecraftAI(boolean use) {
        data().setPersistent(NPC.Metadata.USE_MINECRAFT_AI, use);
    }

    private void teleport(final Entity entity, Location location, int delay, TeleportCause cause) {
        final Entity passenger = entity.getPassenger();
        entity.eject();
        if (!location.getWorld().equals(entity.getWorld())) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(),
                    () -> entity.teleport(location, cause), delay++);
        } else {
            entity.teleport(location, cause);
        }
        if (passenger == null)
            return;
        teleport(passenger, location, delay++, cause);
        Runnable task = () -> entity.setPassenger(passenger);
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
        NPCTeleportEvent event = new NPCTeleportEvent(this, location);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        Entity entity = getEntity();
        while (entity.getVehicle() != null) {
            entity = entity.getVehicle();
        }
        location.getBlock().getChunk();
        teleport(entity, location, 5, cause);
    }

    protected void unloadEvents() {
        runnables.clear();
        for (Trait trait : traits.values()) {
            HandlerList.unregisterAll(trait);
        }
        traits.clear();
        goalController.clear();
    }

    public void update() {
        for (int i = 0; i < runnables.size(); ++i) {
            runnables.get(i).run();
        }
        if (isSpawned()) {
            goalController.run();
        }
    }

    @Override
    public void updateCustomName() {
        getEntity().setCustomName(getFullName());
    }

    @Override
    public boolean useMinecraftAI() {
        return data().get(NPC.Metadata.USE_MINECRAFT_AI, false);
    }
}
