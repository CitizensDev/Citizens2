package net.citizensnpcs;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.event.CitizensDisableEvent;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.CitizensReloadEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.scripting.EventRegistrar;
import net.citizensnpcs.api.scripting.ObjectProvider;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.CommandManager;
import net.citizensnpcs.command.CommandManager.CommandInfo;
import net.citizensnpcs.command.Injector;
import net.citizensnpcs.command.command.AdminCommands;
import net.citizensnpcs.command.command.EditorCommands;
import net.citizensnpcs.command.command.HelpCommands;
import net.citizensnpcs.command.command.NPCCommands;
import net.citizensnpcs.command.command.ScriptCommands;
import net.citizensnpcs.command.command.TraitCommands;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.command.exception.CommandUsageException;
import net.citizensnpcs.command.exception.ServerCommandException;
import net.citizensnpcs.command.exception.UnhandledCommandException;
import net.citizensnpcs.command.exception.WrappedCommandException;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.CitizensNPCRegistry;
import net.citizensnpcs.npc.CitizensTraitFactory;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Translator;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Iterables;

public class Citizens extends JavaPlugin implements CitizensPlugin {
    private final CommandManager commands = new CommandManager();
    private boolean compatible;
    private Settings config;
    private ClassLoader contextClassLoader;
    private CitizensNPCRegistry npcRegistry;
    private NPCDataStore saves;
    private NPCSelector selector;
    private CitizensTraitFactory traitFactory;

    private void despawnNPCs() {
        Iterator<NPC> itr = npcRegistry.iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            npc.despawn();
            itr.remove();
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
                Messaging.logF("Loading %s", plugin.getDescription().getFullName());
                plugin.onLoad();
            } catch (Throwable ex) {
                Messaging.severe(ex.getMessage() + " initializing " + plugin.getDescription().getFullName());
                ex.printStackTrace();
            }
        }
        ((CraftServer) Bukkit.getServer()).enablePlugins(PluginLoadOrder.POSTWORLD);
    }

    public Iterable<CommandInfo> getCommands(String base) {
        return commands.getCommands(base);
    }

    @Override
    public NPCRegistry getNPCRegistry() {
        return npcRegistry;
    }

    public NPCSelector getNPCSelector() {
        return selector;
    }

    @Override
    public File getScriptFolder() {
        return new File(getDataFolder(), "scripts");
    }

    @Override
    public TraitFactory getTraitFactory() {
        return traitFactory;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {
        try {
            // must put command into split.
            String[] split = new String[args.length + 1];
            System.arraycopy(args, 0, split, 1, args.length);
            split[0] = cmd.getName().toLowerCase();

            String modifier = args.length > 0 ? args[0] : "";

            if (!commands.hasCommand(split[0], modifier) && !modifier.isEmpty()) {
                return suggestClosestModifier(sender, split[0], modifier);
            }

            NPC npc = selector.getSelected(sender);
            // TODO: change the args supplied to a context style system for
            // flexibility (ie. adding more context in the future without
            // changing everything)
            try {
                commands.execute(split, sender, sender, npc);
            } catch (ServerCommandException ex) {
                Messaging.send(sender, "You must be in-game to execute that command.");
            } catch (CommandUsageException ex) {
                Messaging.sendError(sender, ex.getMessage());
                Messaging.sendError(sender, ex.getUsage());
            } catch (WrappedCommandException ex) {
                throw ex.getCause();
            } catch (UnhandledCommandException ex) {
                return false;
            } catch (CommandException ex) {
                Messaging.sendError(sender, ex.getMessage());
            }
        } catch (NumberFormatException ex) {
            Messaging.sendError(sender, "That is not a valid number.");
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (sender instanceof Player) {
                Messaging.sendError(sender, "Please report this error: [See console]");
                Messaging.sendError(sender, ex.getClass().getName() + ": " + ex.getMessage());
            }
        }
        return true;
    }

    @Override
    public void onDisable() {
        Bukkit.getPluginManager().callEvent(new CitizensDisableEvent());
        Editor.leaveAll();
        CitizensAPI.shutdown();

        tearDownScripting();
        // Don't bother with this part if MC versions are not compatible
        if (compatible) {
            saves.storeAll(npcRegistry);
            saves.saveToDiskImmediate();
            despawnNPCs();
            npcRegistry = null;
        }

        Messaging.logF("v%s disabled.", getDescription().getVersion());
    }

    @Override
    public void onEnable() {
        // Disable if the server is not using the compatible Minecraft version
        String mcVersion = ((CraftServer) getServer()).getServer().getVersion();
        compatible = mcVersion.startsWith(COMPATIBLE_MC_VERSION);
        if (!compatible) {
            Messaging.severeF("v%s is not compatible with Minecraft v%s. Disabling.", getDescription()
                    .getVersion(), mcVersion);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        registerScriptHelpers();

        config = new Settings(getDataFolder());
        saves = NPCDataStore.create(getDataFolder());
        if (saves == null) {
            Messaging.severeTr(Messages.FAILED_LOAD_SAVES);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        npcRegistry = new CitizensNPCRegistry(saves);
        traitFactory = new CitizensTraitFactory();
        selector = new NPCSelector(this);
        CitizensAPI.setImplementation(this);

        getServer().getPluginManager().registerEvents(new EventListen(), this);

        if (Setting.NPC_COST.asDouble() > 0)
            setupEconomy();

        registerCommands();
        setupTranslator();
        Messaging.logF("v%s enabled.", getDescription().getVersion());

        // Setup NPCs after all plugins have been enabled (allows for multiworld
        // support and for NPCs to properly register external settings)
        if (getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                saves.loadInto(npcRegistry);
                startMetrics();
                enableSubPlugins();
                scheduleSaveTask(Setting.SAVE_TASK_DELAY.asInt());
                Bukkit.getPluginManager().callEvent(new CitizensEnableEvent());
            }
        }) == -1) {
            Messaging.severe("NPC load task couldn't be scheduled - disabling...");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onImplementationChanged() {
        Messaging.severeTr(Messages.CITIZENS_IMPLEMENTATION_DISABLED);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    private void registerCommands() {
        commands.setInjector(new Injector(this));

        // Register command classes
        commands.register(AdminCommands.class);
        commands.register(EditorCommands.class);
        commands.register(HelpCommands.class);
        commands.register(NPCCommands.class);
        commands.register(ScriptCommands.class);
        commands.register(TraitCommands.class);
    }

    private void registerScriptHelpers() {
        setupScripting();
        ScriptCompiler compiler = CitizensAPI.getScriptCompiler();
        compiler.registerGlobalContextProvider(new EventRegistrar(this));
        compiler.registerGlobalContextProvider(new ObjectProvider("plugin", this));
    }

    public void reload() throws NPCLoadException {
        Editor.leaveAll();
        config.reload();
        despawnNPCs();
        saves.loadInto(npcRegistry);

        getServer().getPluginManager().callEvent(new CitizensReloadEvent());
    }

    private void scheduleSaveTask(int delay) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                storeNPCs();
                saves.saveToDisk();
            }
        });
    }

    private void setupEconomy() {
        try {
            RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(
                    Economy.class);
            if (provider != null && provider.getProvider() != null) {
                Economy economy = provider.getProvider();
                Bukkit.getPluginManager().registerEvents(new PaymentListener(economy), this);
            }
        } catch (NoClassDefFoundError e) {
            Messaging.log("Unable to use economy handling. Has Vault been enabled?");
        }
    }

    private void setupScripting() {
        contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());
        // Workaround to fix scripts not loading plugin classes properly.
        // The built in Sun Rhino Javascript engine uses the context classloader
        // to search for class imports. Since the context classloader only has
        // CraftBukkit classes, we replace it with a PluginClassLoader, which
        // allows all plugin classes to be imported.
    }

    private void setupTranslator() {
        Locale locale = Locale.getDefault();
        String[] parts = Setting.LOCALE.asString().split("[\\._]");
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
        Translator.setInstance(new File(getDataFolder(), "i18n"), locale);
        Messaging.logF("Using locale %s.", locale);
    }

    private void startMetrics() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Metrics metrics = new Metrics(Citizens.this);
                    if (metrics.isOptOut())
                        return;
                    metrics.addCustomData(new Metrics.Plotter("Total NPCs") {
                        @Override
                        public int getValue() {
                            return Iterables.size(npcRegistry);
                        }
                    });

                    traitFactory.addPlotters(metrics.createGraph("traits"));
                    metrics.start();
                    Messaging.log("Metrics started.");
                } catch (IOException e) {
                    Messaging.logF("Unable to start metrics: %s.", e.getMessage());
                }
            }
        }.start();
    }

    public void storeNPCs() {
        if (saves == null)
            return;
        for (NPC npc : npcRegistry)
            saves.store(npc);
    }

    public void storeNPCs(CommandContext args) {
        storeNPCs();
        boolean async = args.hasFlag('a');
        if (async)
            saves.saveToDisk();
        else
            saves.saveToDiskImmediate();
    }

    private boolean suggestClosestModifier(CommandSender sender, String command, String modifier) {
        int minDist = Integer.MAX_VALUE;
        String closest = "";
        for (String string : commands.getAllCommandModifiers(command)) {
            int distance = StringHelper.getLevenshteinDistance(modifier, string);
            if (minDist > distance) {
                minDist = distance;
                closest = string;
            }
        }
        if (!closest.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Unknown command. Did you mean:");
            sender.sendMessage(StringHelper.wrap(" /") + command + " " + StringHelper.wrap(closest));
            return true;
        }
        return false;
    }

    private void tearDownScripting() {
        if (Thread.currentThread().getContextClassLoader() == getClassLoader())
            Thread.currentThread().setContextClassLoader(contextClassLoader);
    }

    private static final String COMPATIBLE_MC_VERSION = "1.3";
}