package net.citizensnpcs;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;

import ch.ethz.globis.phtree.PhTreeHelper;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import net.byteflux.libby.logging.LogLevel;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.LocationLookup;
import net.citizensnpcs.api.NMSHelper;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.tree.BehaviorRegistry;
import net.citizensnpcs.api.astar.pathfinder.AsyncChunkCache;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.Injector;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.CitizensPreReloadEvent;
import net.citizensnpcs.api.event.CitizensReloadEvent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.expr.ExpressionRegistry;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.SimpleNPCDataStore;
import net.citizensnpcs.api.npc.templates.TemplateRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.api.util.SpigotUtil.InventoryViewAPI;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.Translator;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.api.util.schedulers.SchedulerTask;
import net.citizensnpcs.commands.AdminCommands;
import net.citizensnpcs.commands.EditorCommands;
import net.citizensnpcs.commands.NPCCommands;
import net.citizensnpcs.commands.TemplateCommands;
import net.citizensnpcs.commands.TraitCommands;
import net.citizensnpcs.commands.WaypointCommands;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.CitizensNPCRegistry;
import net.citizensnpcs.npc.CitizensTraitFactory;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.npc.ai.tree.CitizensBehaviorRegistry;
import net.citizensnpcs.npc.ai.tree.MolangEngine;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.npc.skin.profile.ProfileFetcher;
import net.citizensnpcs.trait.shop.StoredShops;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerUpdateTask;
import net.citizensnpcs.util.SkinProperty;
import net.citizensnpcs.util.Util;
import net.milkbowl.vault.economy.Economy;

public class Citizens extends JavaPlugin implements CitizensPlugin {
    private final List<NPCRegistry> anonymousRegistries = Lists.newArrayList();
    private AsyncChunkCache asyncChunkCache;
    private BehaviorRegistry behaviorRegistry;
    private final CommandManager commands = new CommandManager();
    private Settings config;
    private DenizenHook denizenHook;
    private boolean enabled;
    private ExpressionRegistry expressionRegistry;
    private LocationLookup locationLookup;
    private final NMSHelper nmsHelper = new NMSHelper() {
        private boolean SUPPORT_OWNER_PROFILE = false;
        {
            try {
                SkullMeta.class.getMethod("getOwnerProfile");
                SUPPORT_OWNER_PROFILE = true;
            } catch (Exception e) {
            }
        }

        @Override
        public OfflinePlayer getPlayer(BlockCommandSender sender) {
            Entity entity = NMS.getSource(sender);
            return entity instanceof OfflinePlayer ? (OfflinePlayer) entity : null;
        }

        @Override
        public String getTexture(SkullMeta meta) {
            SkinProperty sp = SkinProperty.fromMojangProfile(NMS.getProfile(meta));
            return sp == null ? null : sp.value;
        }

        @Override
        public InventoryViewAPI openAnvilInventory(Player player, Inventory inventory, String title) {
            return new InventoryViewAPI(NMS.openAnvilInventory(player, inventory, title));
        }

        @Override
        public void setTexture(String texture, SkullMeta meta) {
            GameProfile profile = NMS.getProfile(meta);
            if (profile == null) {
                if (SUPPORT_OWNER_PROFILE) {
                    profile = new GameProfile(meta.getOwnerProfile().getUniqueId(), meta.getOwnerProfile().getName());
                } else {
                    profile = new GameProfile(UUID.randomUUID(), null);
                }
            }
            NMS.setProfile(meta, new SkinProperty("textures", texture, null).applyProperties(profile));
        }

        @Override
        public void updateInventoryTitle(Player player, InventoryViewAPI view, String newTitle) {
            Inventory top = view.getTopInventory();
            if (top.getType() == InventoryType.CRAFTING || top.getType() == InventoryType.CREATIVE
                    || top.getType() == InventoryType.PLAYER)
                return;
            NMS.updateInventoryTitle(player, view, newTitle);
        }
    };
    private CitizensNPCRegistry npcRegistry;
    private boolean packetEventsEnabled;
    private PacketEventsHook packetEventsHook;
    private SchedulerTask playerUpdateTask;
    private boolean saveOnDisable = true;
    private NPCDataStore saves;
    private NPCSelector selector;
    private StoredShops shops;
    private final Map<String, NPCRegistry> storedRegistries = Maps.newHashMap();
    private TemplateRegistry templateRegistry;
    private NPCRegistry temporaryRegistry;

    private CitizensTraitFactory traitFactory;

    @Override
    public NPCRegistry createAnonymousNPCRegistry(NPCDataStore store) {
        CitizensNPCRegistry anon = new CitizensNPCRegistry(store, "anonymous-" + UUID.randomUUID().toString());
        anonymousRegistries.add(anon);
        return anon;
    }

    @Override
    public NPCRegistry createNamedNPCRegistry(String name, NPCDataStore store) {
        NPCRegistry created = new CitizensNPCRegistry(store, name);
        storedRegistries.put(name, created);
        return created;
    }

    private NPCDataStore createStorage(File folder) {
        Storage saves = new YamlStorage(new File(folder, Setting.STORAGE_FILE.asString()), "Citizens NPC Storage");
        if (!saves.load())
            return null;

        return SimpleNPCDataStore.create(saves);
    }

    private void despawnNPCs(boolean save) {
        for (NPCRegistry registry : Arrays.asList(npcRegistry, temporaryRegistry)) {
            if (registry == null)
                continue;

            if (save) {
                if (registry == npcRegistry) {
                    storeNPCsNow();
                } else {
                    registry.saveToStore();
                }
            }
            if (SpigotUtil.isFoliaServer() && !isEnabled())
                return;
            registry.despawnNPCs(DespawnReason.RELOAD);
        }
    }

    @Override
    public AsyncChunkCache getAsyncChunkCache() {
        if (asyncChunkCache == null) {
            // TODO: should parallelism be configurable? or too confusing?
            asyncChunkCache = new AsyncChunkCache(this, Runtime.getRuntime().availableProcessors() > 12 ? 4 : 2,
                    Setting.CITIZENS_PATHFINDER_ASYNC_CHUNK_CACHE_TTL.asDuration().toMillis());
        }
        return asyncChunkCache;
    }

    @Override
    public BehaviorRegistry getBehaviorRegistry() {
        return behaviorRegistry;
    }

    @Override
    public CommandManager getCommandManager() {
        return commands;
    }

    public NPCDataStore getDefaultNPCDataStore() {
        return saves;
    }

    @Override
    public NPCSelector getDefaultNPCSelector() {
        return selector;
    }

    @Override
    public ExpressionRegistry getExpressionRegistry() {
        return expressionRegistry;
    }

    @Override
    public LocationLookup getLocationLookup() {
        return locationLookup;
    }

    @Override
    public NPCRegistry getNamedNPCRegistry(String name) {
        if (name.equals(npcRegistry.getName()))
            return npcRegistry;
        return storedRegistries.get(name);
    }

    @Override
    public NMSHelper getNMSHelper() {
        return nmsHelper;
    }

    @Override
    public Iterable<NPCRegistry> getNPCRegistries() {
        return () -> new Iterator<NPCRegistry>() {
            Iterator<NPCRegistry> stored;

            @Override
            public boolean hasNext() {
                return stored == null ? true : stored.hasNext();
            }

            @Override
            public NPCRegistry next() {
                if (stored == null) {
                    stored = Iterables
                            .concat(storedRegistries.values(), anonymousRegistries, Arrays.asList(temporaryRegistry))
                            .iterator();
                    return npcRegistry;
                }
                return stored.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public NPCRegistry getNPCRegistry() {
        return npcRegistry;
    }

    public NPCSelector getNPCSelector() {
        return selector;
    }

    @Override
    public ClassLoader getOwningClassLoader() {
        return getClassLoader();
    }

    public PacketEventsHook getPacketEventsListener() {
        return packetEventsHook;
    }

    public StoredShops getShops() {
        return shops;
    }

    @Override
    public TemplateRegistry getTemplateRegistry() {
        return templateRegistry;
    }

    @Override
    public NPCRegistry getTemporaryNPCRegistry() {
        return temporaryRegistry;
    }

    @Override
    public TraitFactory getTraitFactory() {
        return traitFactory;
    }

    private void initialiseBehaviorRegistry() {
        expressionRegistry = new ExpressionRegistry();
        expressionRegistry.registerEngine(new MolangEngine());
        behaviorRegistry = new CitizensBehaviorRegistry(expressionRegistry);
    }

    private void loadAdventure() {
        LibraryManager lib = new BukkitLibraryManager(this);
        lib.addMavenCentral();
        lib.setLogLevel(LogLevel.INFO);
        // Unfortunately, transitive dependency management is not supported in this library.

        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-text-minimessage")
                .version("4.25.0").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-api").version("4.25.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-key").version("4.25.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("examination-api").version("1.3.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("examination-string").version("1.3.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-platform-bukkit").version("4.4.1")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-platform-api").version("4.4.1")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-text-serializer-bungeecord")
                .version("4.4.1").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-text-serializer-legacy")
                .version("4.25.0").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-nbt").version("4.25.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-text-serializer-gson")
                .version("4.25.0").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-text-serializer-json")
                .version("4.25.0").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("option").version("1.1.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("org{}jspecify").artifactId("jspecify").version("1.0.0").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-text-serializer-commons")
                .version("4.25.0").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-text-serializer-gson-legacy-impl")
                .version("4.25.0").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-text-serializer-json-legacy-impl")
                .version("4.25.0").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-platform-facet").version("4.4.1")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-platform-viaversion")
                .version("4.4.1").relocate("net{}kyori", "clib{}net{}kyori").build());
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String cmdName, String[] args) {
        Object[] methodArgs = { sender, selector == null ? null : selector.getSelected(sender) };
        return commands.executeSafe(command, args, sender, methodArgs);
    }

    public void onDependentPluginDisable() {
        if (enabled) {
            storeNPCsNow();
            saveOnDisable = false;
        }
    }

    @Override
    public void onDisable() {
        if (!enabled)
            return;

        Editor.leaveAll();
        despawnNPCs(saveOnDisable);
        HandlerList.unregisterAll(this);

        behaviorRegistry = null;
        templateRegistry = null;
        npcRegistry = null;
        locationLookup = null;
        if (asyncChunkCache != null) {
            asyncChunkCache.shutdown();
            asyncChunkCache = null;
        }
        enabled = false;
        saveOnDisable = true;
        ProfileFetcher.shutdown();
        Skin.clearCache();
        NMS.shutdown();
        CitizensAPI.shutdown();
        if (packetEventsEnabled) {
            PacketEvents.getAPI().terminate();
        }
    }

    @Override
    public void onEnable() {
        loadAdventure();
        PhTreeHelper.enablePooling(false);
        PhTreeHelper.ARRAY_POOLING_POOL_SIZE = 0;
        PhTreeHelper.ARRAY_POOLING_MAX_ARRAY_SIZE = 0;
        PhTreeHelper.MAX_OBJECT_POOL_SIZE = 0;

        CitizensAPI.setImplementation(this);
        config = new Settings(getDataFolder());
        setupTranslator();
        // Disable if the server is not using the compatible Minecraft version
        try {
            NMS.loadBridge();
        } catch (Exception e) {
            if (Messaging.isDebugging()) {
                e.printStackTrace();
            }
            Messaging.severeTr(Messages.CITIZENS_INCOMPATIBLE, getDescription().getVersion());
            NMS.shutdown();
            CitizensAPI.shutdown();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        saves = createStorage(getDataFolder());
        initialiseBehaviorRegistry();
        shops = new StoredShops(new YamlStorage(new File(getDataFolder(), "shops.yml"), "Citizens NPC Shops"));
        if (saves == null || !shops.loadFromDisk()) {
            Messaging.severeTr(Messages.FAILED_LOAD_SAVES);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        npcRegistry = new CitizensNPCRegistry(saves, "citizens");
        temporaryRegistry = new CitizensNPCRegistry(new MemoryNPCDataStore(), "citizens-temporary");
        locationLookup = new LocationLookup(npcRegistry);
        locationLookup.runTaskTimer(CitizensAPI.getPlugin(), 0, 5);

        traitFactory = new CitizensTraitFactory(this);
        selector = new NPCSelector(this);

        saveResource("templates/citizens/templates.yml", true);
        templateRegistry = new TemplateRegistry(new File(getDataFolder(), "templates").toPath());

        if (!new File(getDataFolder(), "skins").exists()) {
            new File(getDataFolder(), "skins").mkdir();
        }
        Bukkit.getPluginManager().registerEvents(new EventListen(this), this);
        Bukkit.getPluginManager().registerEvents(new Placeholders(), this);

        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (papi != null && papi.isEnabled()) {
            new CitizensPlaceholders(selector).register();
        }
        setupEconomy();

        registerCommands();
        NMS.load(commands);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        commands.registerTabCompletion(this);
        commands.setTranslationPrefixProvider(cmd -> "citizens.commands." + cmd.aliases()[0]
                + (cmd.modifiers().length > 0 && !cmd.modifiers()[0].isEmpty() ? "." + cmd.modifiers()[0] : ""));

        // Setup NPCs after all plugins have been enabled (allows for multiworld
        // support and for NPCs to properly register external settings)
        if (CitizensAPI.getScheduler().runTaskLater(new CitizensLoadTask(), 1) == null) {
            Messaging.severeTr(Messages.LOAD_TASK_NOT_SCHEDULED);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onImplementationChanged() {
        Messaging.severeTr(Messages.CITIZENS_IMPLEMENTATION_DISABLED);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public void onLoad() {
        if (SpigotUtil.isFoliaServer())
            // Packet rewriting cannot be supported on Folia, because to call entities,
            // it must be done on their thread, so there will be a 1-tick delay,
            // therefore it is not currently supported.
            return;

        try {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().load();
            packetEventsEnabled = true;
        } catch (Throwable t) {
        }
    }

    public void registerCommandClass(Class<?> clazz) {
        try {
            commands.register(clazz);
        } catch (Throwable ex) {
            Messaging.logTr(Messages.CITIZENS_INVALID_COMMAND_CLASS);
            ex.printStackTrace();
        }
    }

    private void registerCommands() {
        commands.setInjector(new Injector(this));
        // Register command classes
        commands.register(AdminCommands.class);
        commands.register(EditorCommands.class);
        commands.register(NPCCommands.class);
        commands.register(TemplateCommands.class);
        commands.register(TraitCommands.class);
        commands.register(WaypointCommands.class);
    }

    public void reload() throws NPCLoadException {
        getServer().getPluginManager().callEvent(new CitizensPreReloadEvent());

        playerUpdateTask.cancel();
        Editor.leaveAll();
        config.reload();
        despawnNPCs(false);
        ProfileFetcher.reset();
        Skin.clearCache();

        templateRegistry = new TemplateRegistry(new File(getDataFolder(), "templates").toPath());

        saves.reloadFromSource();
        saves.loadInto(npcRegistry);

        shops.loadFromDisk();
        shops.load();

        playerUpdateTask = new PlayerUpdateTask().runTaskTimer(Citizens.this, 0, 1);

        getServer().getPluginManager().callEvent(new CitizensReloadEvent());
    }

    @Override
    public void removeNamedNPCRegistry(String name) {
        storedRegistries.remove(name);
    }

    private void scheduleSaveTask(int delay) {
        CitizensAPI.getScheduler().runTaskTimer(new CitizensSaveTask(), delay, delay);
    }

    @Override
    public void setDefaultNPCDataStore(NPCDataStore store) {
        if (store == null)
            throw new IllegalArgumentException("must be non-null");
        despawnNPCs(true);
        saves = store;
        npcRegistry = new CitizensNPCRegistry(saves, "citizens-global-" + UUID.randomUUID().toString());
        saves.loadInto(npcRegistry);
    }

    private void setupEconomy() {
        try {
            RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (provider != null && provider.getProvider() != null) {
                Economy economy = provider.getProvider();
                Bukkit.getPluginManager().registerEvents(new PaymentHook(economy), this);
            }
            Messaging.logTr(Messages.LOADED_ECONOMY);
        } catch (NoClassDefFoundError e) {
        }
    }

    private void setupTranslator() {
        Locale locale = Locale.getDefault();
        String setting = Setting.LOCALE.asString();
        if (!setting.isEmpty()) {
            String[] parts = setting.split("[\\._]");
            switch (parts.length) {
                case 1:
                    locale = new Locale(parts[0]);
                    break;
                case 2:
                    locale = new Locale(parts[0], parts[1]);
                    break;
                case 3:
                    locale = new Locale(parts[0], parts[1], parts[2]);
                    break;
                default:
                    break;
            }
        }
        Translator.setInstance(new File(getDataFolder(), "lang"), locale);
        if (!locale.getLanguage().equals("en")) {
            Messaging.logTr(Messages.CONTRIBUTE_TO_TRANSLATION_PROMPT, locale.getLanguage());
        }
    }

    private void startMetrics() {
        try {
            Metrics metrics = new Metrics(this, 2463);
            metrics.addCustomChart(new Metrics.SingleLineChart("total_npcs",
                    () -> npcRegistry == null ? 0 : Iterables.size(npcRegistry)));
            metrics.addCustomChart(new Metrics.SingleLineChart("total_templates",
                    () -> npcRegistry == null ? 0
                            : (int) templateRegistry.getAllTemplates().stream()
                                    .filter(t -> !t.getKey().getNamespace().equals("citizens")).count()));
            metrics.addCustomChart(new Metrics.SimplePie("locale", () -> Locale.getDefault().getLanguage()));
            metrics.addCustomChart(new Metrics.AdvancedPie("traits", () -> {
                Map<String, Integer> res = Maps.newHashMap();
                for (NPC npc : npcRegistry) {
                    for (Trait trait : npc.getTraits()) {
                        if (traitFactory.trackStats(trait)) {
                            res.put(trait.getName(), res.getOrDefault(trait.getName(), 0) + 1);
                        }
                    }
                }
                return res;
            }));
        } catch (Exception e) {
            Messaging.logTr(Messages.METRICS_ERROR_NOTIFICATION, e.getMessage());
        }
    }

    public void storeNPCs() {
        if (saves == null)
            return;
        saves.storeAll(npcRegistry);
        shops.storeShops();
        shops.saveToDisk();
        saves.saveToDisk();
    }

    public void storeNPCsNow() {
        if (saves == null)
            return;
        saves.storeAll(npcRegistry);
        shops.storeShops();
        shops.saveToDiskImmediate();
        saves.saveToDiskImmediate();
    }

    @Override
    public void talk(SpeechContext context) {
        Util.talk(context);
    }

    private class CitizensLoadTask implements Runnable {

        @Override
        public void run() {
            if (packetEventsEnabled) {
                try {
                    packetEventsHook = new PacketEventsHook(Citizens.this);
                } catch (Throwable t) {
                    Messaging.severe("PacketEvents support not enabled due to following error:");
                    t.printStackTrace();
                }
            }
            if (Bukkit.getPluginManager().getPlugin("Denizen") != null
                    && Bukkit.getPluginManager().getPlugin("Denizen").isEnabled()) {
                try {
                    denizenHook = new DenizenHook(Citizens.this);
                } catch (Throwable t) {
                    Messaging.severe("Denizen support not enabled due to following error:");
                    t.printStackTrace();
                }
            }
            saves.loadInto(npcRegistry);
            shops.load();

            Messaging.logTr(Messages.NUM_LOADED_NOTIFICATION, Iterables.size(npcRegistry), "?");
            startMetrics();
            scheduleSaveTask(Setting.SAVE_TASK_FREQUENCY.asTicks());
            Bukkit.getPluginManager().callEvent(new CitizensEnableEvent());
            playerUpdateTask = new PlayerUpdateTask().runTaskTimer(Citizens.this, 0, 1);
            enabled = true;
        }
    }

    private class CitizensSaveTask implements Runnable {
        @Override
        public void run() {
            storeNPCs();
        }
    }
}
