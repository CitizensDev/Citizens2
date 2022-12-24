package net.citizensnpcs;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import net.byteflux.libby.logging.LogLevel;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.InventoryHelper;
import net.citizensnpcs.api.LocationLookup;
import net.citizensnpcs.api.SkullMetaProvider;
import net.citizensnpcs.api.ai.speech.SpeechFactory;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.Injector;
import net.citizensnpcs.api.event.CitizensDisableEvent;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.CitizensPreReloadEvent;
import net.citizensnpcs.api.event.CitizensReloadEvent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.SimpleNPCDataStore;
import net.citizensnpcs.api.scripting.EventRegistrar;
import net.citizensnpcs.api.scripting.ObjectProvider;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.NBTStorage;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.Translator;
import net.citizensnpcs.api.util.YamlStorage;
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
import net.citizensnpcs.npc.Template;
import net.citizensnpcs.npc.ai.speech.CitizensSpeechFactory;
import net.citizensnpcs.npc.profile.ProfileFetcher;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.trait.ShopTrait;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerUpdateTask;
import net.citizensnpcs.util.Util;
import net.milkbowl.vault.economy.Economy;

public class Citizens extends JavaPlugin implements CitizensPlugin {
    private final List<NPCRegistry> anonymousRegistries = Lists.newArrayList();
    private final List<NPCRegistry> citizensBackedRegistries = Lists.newArrayList();
    private final CommandManager commands = new CommandManager();
    private Settings config;
    private boolean enabled;
    private final InventoryHelper inventoryHelper = new InventoryHelper() {
        @Override
        public InventoryView openAnvilInventory(Player player, Inventory inventory, String title) {
            return NMS.openAnvilInventory(player, inventory, title);
        }

        @Override
        public void updateInventoryTitle(Player player, InventoryView view, String newTitle) {
            if (view.getTopInventory().getType() == InventoryType.CRAFTING
                    || view.getTopInventory().getType() == InventoryType.CREATIVE
                    || view.getTopInventory().getType() == InventoryType.PLAYER)
                return;
            NMS.updateInventoryTitle(player, view, newTitle);
        }
    };
    private LocationLookup locationLookup;
    private CitizensNPCRegistry npcRegistry;
    private ProtocolLibListener protocolListener;
    private boolean saveOnDisable = true;
    private NPCDataStore saves;
    private NPCSelector selector;
    private StoredShops shops;
    private final SkullMetaProvider skullMetaProvider = new SkullMetaProvider() {
        @Override
        public String getTexture(SkullMeta meta) {
            if (NMS.getProfile(meta) == null)
                return null;
            return Iterables.getFirst(NMS.getProfile(meta).getProperties().get("textures"), new Property("", ""))
                    .getValue();
        }

        @Override
        public void setTexture(String string, SkullMeta meta) {
            UUID uuid = meta.getOwningPlayer() == null ? UUID.randomUUID() : meta.getOwningPlayer().getUniqueId();
            NMS.setProfile(meta, new GameProfile(uuid, string));
        }
    };
    private CitizensSpeechFactory speechFactory;
    private final Map<String, NPCRegistry> storedRegistries = Maps.newHashMap();
    private CitizensTraitFactory traitFactory;

    @Override
    public NPCRegistry createAnonymousNPCRegistry(NPCDataStore store) {
        CitizensNPCRegistry anon = new CitizensNPCRegistry(store, "anonymous-" + UUID.randomUUID().toString());
        anonymousRegistries.add(anon);
        return anon;
    }

    @Override
    public NPCRegistry createCitizensBackedNPCRegistry(NPCDataStore store) {
        CitizensNPCRegistry anon = new CitizensNPCRegistry(store, "anonymous-citizens-" + UUID.randomUUID().toString());
        citizensBackedRegistries.add(anon);
        return anon;
    }

    @Override
    public NPCRegistry createNamedNPCRegistry(String name, NPCDataStore store) {
        NPCRegistry created = new CitizensNPCRegistry(store, name);
        storedRegistries.put(name, created);
        return created;
    }

    private NPCDataStore createStorage(File folder) {
        Storage saves = null;
        String type = Setting.STORAGE_TYPE.asString();
        if (type.equalsIgnoreCase("nbt")) {
            saves = new NBTStorage(new File(folder + File.separator + Setting.STORAGE_FILE.asString()),
                    "Citizens NPC Storage");
        }
        if (saves == null) {
            saves = new YamlStorage(new File(folder, Setting.STORAGE_FILE.asString()), "Citizens NPC Storage");
        }
        if (!saves.load())
            return null;
        return SimpleNPCDataStore.create(saves);
    }

    private void despawnNPCs(boolean save) {
        for (NPCRegistry registry : Iterables.concat(Arrays.asList(npcRegistry), citizensBackedRegistries)) {
            if (registry == null)
                continue;
            if (save) {
                if (registry == npcRegistry) {
                    storeNPCs(false);
                } else {
                    registry.saveToStore();
                }
            }
            registry.despawnNPCs(DespawnReason.RELOAD);
        }
    }

    private void enableSubPlugins() {
        File root = new File(getDataFolder(), Setting.SUBPLUGIN_FOLDER.asString());
        if (!root.exists() || !root.isDirectory())
            return;
        File[] files = root.listFiles();
        for (File file : files) {
            Plugin plugin;
            try {
                plugin = Bukkit.getPluginManager().loadPlugin(file);
            } catch (Exception e) {
                continue;
            }
            if (plugin == null)
                continue;
            // code beneath modified from CraftServer
            try {
                Messaging.logTr(Messages.LOADING_SUB_PLUGIN, plugin.getDescription().getFullName());
                plugin.onLoad();
            } catch (Throwable ex) {
                Messaging.severeTr(Messages.ERROR_INITALISING_SUB_PLUGIN, ex.getMessage(),
                        plugin.getDescription().getFullName());
                ex.printStackTrace();
            }
        }
        NMS.loadPlugins();
    }

    @Override
    public CommandManager getCommandManager() {
        return commands;
    }

    @Override
    public net.citizensnpcs.api.npc.NPCSelector getDefaultNPCSelector() {
        return selector;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
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
    public Iterable<NPCRegistry> getNPCRegistries() {
        return new Iterable<NPCRegistry>() {
            @Override
            public Iterator<NPCRegistry> iterator() {
                return new Iterator<NPCRegistry>() {
                    Iterator<NPCRegistry> stored;

                    @Override
                    public boolean hasNext() {
                        return stored == null ? true : stored.hasNext();
                    }

                    @Override
                    public NPCRegistry next() {
                        if (stored == null) {
                            stored = Iterables
                                    .concat(storedRegistries.values(), anonymousRegistries, citizensBackedRegistries)
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

    @Override
    public File getScriptFolder() {
        return new File(getDataFolder(), "scripts");
    }

    public StoredShops getShops() {
        return shops;
    }

    @Override
    public SkullMetaProvider getSkullMetaProvider() {
        return skullMetaProvider;
    }

    @Override
    public SpeechFactory getSpeechFactory() {
        return speechFactory;
    }

    @Override
    public TraitFactory getTraitFactory() {
        return traitFactory;
    }

    private void loadMavenLibraries() {
        getLogger().info("Loading external libraries");

        LibraryManager lib = new BukkitLibraryManager(this);
        lib.addMavenCentral();
        lib.setLogLevel(LogLevel.WARN);
        // Unfortunately, transitive dependency management is not supported in this library.
        lib.loadLibrary(Library.builder().groupId("ch{}ethz{}globis{}phtree").artifactId("phtree").version("2.5.0")
                .relocate("ch{}ethz{}globis{}phtree", "clib{}phtree").build());
        lib.loadLibrary(Library.builder().groupId("net{}sf{}trove4j").artifactId("trove4j").version("3.0.3")
                .relocate("gnu{}trove", "clib{}trove").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-text-minimessage")
                .version("4.12.0").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-platform-bukkit").version("4.2.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-platform-api").version("4.2.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-platform-facet").version("4.2.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-platform-viaversion")
                .version("4.2.0").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-api").version("4.12.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-text-serializer-bungeecord")
                .version("4.2.0").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-text-serializer-legacy")
                .version("4.12.0").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-text-serializer-gson")
                .version("4.12.0").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-text-serializer-gson-legacy-impl")
                .version("4.12.0").relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-nbt").version("4.12.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("adventure-key").version("4.12.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("examination-api").version("1.3.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
        lib.loadLibrary(Library.builder().groupId("net{}kyori").artifactId("examination-string").version("1.3.0")
                .relocate("net{}kyori", "clib{}net{}kyori").build());
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String cmdName, String[] args) {
        Object[] methodArgs = { sender, selector == null ? null : selector.getSelected(sender) };
        return commands.executeSafe(command, args, sender, methodArgs);
    }

    public void onDependentPluginDisable() {
        storeNPCs(false);
        saveOnDisable = false;
    }

    @Override
    public void onDisable() {
        if (!enabled) {
            return;
        }
        Bukkit.getPluginManager().callEvent(new CitizensDisableEvent());
        Editor.leaveAll();
        despawnNPCs(saveOnDisable);
        HandlerList.unregisterAll(this);
        npcRegistry = null;
        locationLookup = null;
        enabled = false;
        saveOnDisable = true;
        Template.shutdown();
        NMS.shutdown();
        CitizensAPI.shutdown();
    }

    @Override
    public void onEnable() {
        loadMavenLibraries();

        CitizensAPI.setImplementation(this);
        config = new Settings(getDataFolder());
        setupTranslator();
        // Disable if the server is not using the compatible Minecraft version
        String mcVersion = Util.getMinecraftRevision();
        try {
            NMS.loadBridge(mcVersion);
        } catch (Exception e) {
            if (Messaging.isDebugging()) {
                e.printStackTrace();
            }
            Messaging.severeTr(Messages.CITIZENS_INCOMPATIBLE, getDescription().getVersion(), mcVersion);
            enabled = true;
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        registerScriptHelpers();

        saves = createStorage(getDataFolder());
        shops = new StoredShops(new YamlStorage(new File(getDataFolder(), "shops.yml")));
        if (saves == null || !shops.loadFromDisk()) {
            Messaging.severeTr(Messages.FAILED_LOAD_SAVES);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        locationLookup = new LocationLookup();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, locationLookup, 0, 5);

        speechFactory = new CitizensSpeechFactory();
        npcRegistry = new CitizensNPCRegistry(saves, "citizens");
        traitFactory = new CitizensTraitFactory();
        traitFactory.registerTrait(TraitInfo.create(ShopTrait.class).withSupplier(() -> {
            return new ShopTrait(shops);
        }));
        selector = new NPCSelector(this);

        Bukkit.getPluginManager().registerEvents(new EventListen(storedRegistries), this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CitizensPlaceholders(selector).register();
        }
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            protocolListener = new ProtocolLibListener(this);
        }

        setupEconomy();

        registerCommands();
        enableSubPlugins();
        NMS.load(commands);
        Template.migrate();
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        commands.registerTabCompletion(this);

        // Setup NPCs after all plugins have been enabled (allows for multiworld
        // support and for NPCs to properly register external settings)
        if (getServer().getScheduler().scheduleSyncDelayedTask(this, new CitizensLoadTask(), 1) == -1) {
            Messaging.severeTr(Messages.LOAD_TASK_NOT_SCHEDULED);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onImplementationChanged() {
        Messaging.severeTr(Messages.CITIZENS_IMPLEMENTATION_DISABLED);
        Bukkit.getPluginManager().disablePlugin(this);
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

    private void registerScriptHelpers() {
        ScriptCompiler compiler = CitizensAPI.getScriptCompiler();
        compiler.registerGlobalContextProvider(new EventRegistrar(this));
        compiler.registerGlobalContextProvider(new ObjectProvider("plugin", this));
    }

    public void reload() throws NPCLoadException {
        Editor.leaveAll();
        config.reload();
        despawnNPCs(false);
        ProfileFetcher.reset();
        Skin.clearCache();
        getServer().getPluginManager().callEvent(new CitizensPreReloadEvent());

        saves.reloadFromSource();
        saves.loadInto(npcRegistry);

        shops.loadFromDisk();
        shops.load();

        Template.shutdown();

        getServer().getPluginManager().callEvent(new CitizensReloadEvent());
    }

    @Override
    public void removeNamedNPCRegistry(String name) {
        storedRegistries.remove(name);
    }

    private void scheduleSaveTask(int delay) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new CitizensSaveTask(), delay, delay);
    }

    @Override
    public void setDefaultNPCDataStore(NPCDataStore store) {
        if (store == null) {
            throw new IllegalArgumentException("must be non-null");
        }
        despawnNPCs(true);
        this.saves = store;
        this.npcRegistry = new CitizensNPCRegistry(saves, "citizens-global-" + UUID.randomUUID().toString());
        saves.loadInto(npcRegistry);
    }

    private void setupEconomy() {
        try {
            RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (provider != null && provider.getProvider() != null) {
                Economy economy = provider.getProvider();
                Bukkit.getPluginManager().registerEvents(new PaymentListener(economy), this);
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
    }

    private void startMetrics() {
        try {
            Metrics metrics = new Metrics(this, 2463);

            metrics.addCustomChart(new Metrics.SingleLineChart("total_npcs", new Callable<Integer>() {
                @Override
                public Integer call() {
                    if (npcRegistry == null)
                        return 0;
                    return Iterables.size(npcRegistry);
                }
            }));
        } catch (Exception e) {
            Messaging.logTr(Messages.METRICS_ERROR_NOTIFICATION, e.getMessage());
        }
    }

    public void storeNPCs() {
        storeNPCs(false);
    }

    public void storeNPCs(boolean async) {
        if (saves == null)
            return;
        saves.storeAll(npcRegistry);
        shops.saveShops();
        if (async) {
            saves.saveToDisk();
            new Thread(() -> {
                shops.saveToDisk();
            }).start();
        } else {
            shops.saveToDisk();
            saves.saveToDiskImmediate();
        }
    }

    private class CitizensLoadTask implements Runnable {
        @Override
        public void run() {
            saves.loadInto(npcRegistry);
            shops.load();

            Messaging.logTr(Messages.NUM_LOADED_NOTIFICATION, Iterables.size(npcRegistry), "?");
            startMetrics();
            scheduleSaveTask(Setting.SAVE_TASK_DELAY.asInt());
            Bukkit.getPluginManager().callEvent(new CitizensEnableEvent());
            new PlayerUpdateTask().runTaskTimer(Citizens.this, 0, 1);
            enabled = true;
        }
    }

    private class CitizensSaveTask implements Runnable {
        @Override
        public void run() {
            storeNPCs(false);
        }
    }
}
