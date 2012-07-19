package net.citizensnpcs;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.event.CitizensReloadEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.scripting.EventRegistrar;
import net.citizensnpcs.api.scripting.ObjectProvider;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.DatabaseStorage;
import net.citizensnpcs.api.util.NBTStorage;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.command.CommandManager;
import net.citizensnpcs.command.Injector;
import net.citizensnpcs.command.command.AdminCommands;
import net.citizensnpcs.command.command.EditorCommands;
import net.citizensnpcs.command.command.HelpCommands;
import net.citizensnpcs.command.command.NPCCommands;
import net.citizensnpcs.command.command.ScriptCommands;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.command.exception.CommandUsageException;
import net.citizensnpcs.command.exception.ServerCommandException;
import net.citizensnpcs.command.exception.UnhandledCommandException;
import net.citizensnpcs.command.exception.WrappedCommandException;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCRegistry;
import net.citizensnpcs.npc.CitizensTraitFactory;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Iterables;

public class Citizens extends JavaPlugin implements CitizensPlugin {
    private final CommandManager commands = new CommandManager();
    private boolean compatible;
    private Settings config;
    private ClassLoader contextClassLoader;
    private CitizensNPCRegistry npcRegistry;
    private Storage saves; // TODO: refactor this, it's used in too many places
    private NPCSelector selector;
    private CitizensTraitFactory traitFactory;

    private void despawnNPCs() {
        Iterator<NPC> itr = npcRegistry.iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            itr.remove();
            npc.despawn();
        }
    }

    private void enableSubPlugins() {
        File root = new File(getDataFolder(), Setting.SUBPLUGIN_FOLDER.asString());
        if (!root.exists() || !root.isDirectory())
            return;
        Plugin[] plugins = Bukkit.getPluginManager().loadPlugins(root);
        // code beneath modified from CraftServer
        for (Plugin plugin : plugins) {
            try {
                Messaging.logF("Loading %s", plugin.getDescription().getFullName());
                plugin.onLoad();
            } catch (Throwable ex) {
                Messaging.severe(ex.getMessage() + " initializing " + plugin.getDescription().getFullName());
                ex.printStackTrace();
            }
        }
    }

    public Iterable<net.citizensnpcs.command.Command> getCommands(String base) {
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

        tearDownScripting();
        // Don't bother with this part if MC versions are not compatible
        if (compatible) {
            save();
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
            Messaging.severeF("v%s is not compatible with Minecraft v%s. Disabling.", getDescription().getVersion(),
                    mcVersion);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        registerScriptHelpers();

        config = new Settings(getDataFolder());

        setupStorage();

        npcRegistry = new CitizensNPCRegistry(saves);
        traitFactory = new CitizensTraitFactory();
        selector = new NPCSelector(this);
        CitizensAPI.setImplementation(this);

        getServer().getPluginManager().registerEvents(new EventListen(), this);

        registerCommands();

        Messaging.logF("v%s enabled.", getDescription().getVersion());

        // Setup NPCs after all plugins have been enabled (allows for multiworld
        // support and for NPCs to properly register external settings)
        if (getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                setupNPCs();
                startMetrics();
                enableSubPlugins();
            }
        }) == -1) {
            Messaging.severe("Issue enabling plugin. Disabling.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onImplementationChanged() {
        Messaging.severe("Citizens implementation changed, disabling plugin.");
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
        setupNPCs();

        getServer().getPluginManager().callEvent(new CitizensReloadEvent());
    }

    public void save() {
        for (NPC npc : npcRegistry)
            ((CitizensNPC) npc).save(saves.getKey("npc." + npc.getId()));

        saves.save();
    }

    // TODO: refactor
    private void setupNPCs() {
        saves.load();
        int created = 0, spawned = 0;
        for (DataKey key : saves.getKey("npc").getIntegerSubKeys()) {
            int id = Integer.parseInt(key.name());
            if (!key.keyExists("name")) {
                Messaging.logF("Could not find a name for the NPC with ID '%s'.", id);
                continue;
            }
            String unparsedEntityType = key.getString("traits.type", "PLAYER");
            EntityType type = EntityType.fromName(unparsedEntityType);
            if (type == null) {
                try {
                    type = EntityType.valueOf(unparsedEntityType);
                } catch (IllegalArgumentException ex) {
                    Messaging.logF("NPC type '%s' was not recognized. Did you spell it correctly?", unparsedEntityType);
                    continue;
                }
            }
            NPC npc = npcRegistry.createNPC(type, id, key.getString("name"));
            ((CitizensNPC) npc).load(key);

            ++created;
            if (npc.isSpawned())
                ++spawned;
        }
        Messaging.logF("Loaded %d NPCs (%d spawned).", created, spawned);
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

    private void setupStorage() {
        String type = Setting.STORAGE_TYPE.asString();
        if (type.equalsIgnoreCase("db") || type.equalsIgnoreCase("database")) {
            try {
                saves = new DatabaseStorage(Setting.DATABASE_DRIVER.asString(), Setting.DATABASE_URL.asString(),
                        Setting.DATABASE_USERNAME.asString(), Setting.DATABASE_PASSWORD.asString());
            } catch (SQLException e) {
                e.printStackTrace();
                Messaging.log("Unable to connect to database, falling back to YAML");
            }
        } else if (type.equalsIgnoreCase("nbt")) {
            saves = new NBTStorage(getDataFolder() + File.separator + Setting.STORAGE_FILE.asString(),
                    "Citizens NPC Storage");
        }
        if (saves == null) {
            saves = new YamlStorage(getDataFolder() + File.separator + Setting.STORAGE_FILE.asString(),
                    "Citizens NPC Storage");
        }
        Messaging.logF("Save method set to %s.", saves.toString());
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
        Thread.currentThread().setContextClassLoader(contextClassLoader);
    }

    private static final String COMPATIBLE_MC_VERSION = "1.2.5";
}