package net.citizensnpcs.bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

import javassist.ClassPool;
import javassist.Loader;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.CitizensDisableEvent;
import net.citizensnpcs.EventListen;
import net.citizensnpcs.Metrics;
import net.citizensnpcs.Settings;
import net.citizensnpcs.Metrics.Plotter;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.abstraction.MobType;
import net.citizensnpcs.api.abstraction.entity.Player;
import net.citizensnpcs.api.event.CitizensReloadEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.scripting.EventRegistrar;
import net.citizensnpcs.api.scripting.ObjectProvider;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.DatabaseStorage;
import net.citizensnpcs.api.util.NBTStorage;
import net.citizensnpcs.api.util.Storage;
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
import net.citizensnpcs.npc.CitizensAttachmentFactory;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCRegistry;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Iterables;

public class CitizensBukkit extends JavaPlugin {
    private final CommandManager commands = new CommandManager();
    private boolean compatible;
    private Citizens implementation;
    private Settings config;
    private ClassLoader contextClassLoader;
    private Storage saves; // TODO: refactor this, it's used in too many places
    private NPCSelector selector;

    public Iterable<net.citizensnpcs.command.Command> getCommands(String base) {
        return commands.getCommands(base);
    }

    public NPCSelector getNPCSelector() {
        return selector;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, String cmdName,
            String[] args) {
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
        CitizensAPI.getServer().callEvent(new CitizensDisableEvent());

        tearDownScripting();
        // Don't bother with this part if MC versions are not compatible
        if (compatible) {
            save();
            despawnNPCs();
        }

        implementation = null;

        Messaging.logF("v%s disabled.", getDescription().getVersion());
    }

    @Override
    public void onEnable() {
        // Disable if the server is not using the compatible Minecraft version
        String mcVersion = CitizensAPI.getServer().getMinecraftVersion();
        compatible = mcVersion.startsWith(COMPATIBLE_MC_VERSION);
        if (!compatible) {
            Messaging.severeF("v%s is not compatible with Minecraft v%s. Disabling.", getDescription().getVersion(),
                    mcVersion);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupClassLoader();
        registerScriptHelpers();

        config = new Settings(new YamlStorage(folder + File.separator + "config.yml", "Citizens Configuration"));

        setupStorage();

        implementation = new Citizens(null, new CitizensNPCRegistry(saves), getDataFolder());
        CitizensAPI.setImplementation(implementation);

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
            }

            private void startMetrics() {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Metrics metrics = new Metrics(CitizensBukkit.this);
                            if (metrics.isOptOut())
                                return;
                            metrics.addCustomData(new Metrics.Plotter() {
                                @Override
                                public int getValue() {
                                    return Iterables.size(implementation.getNPCRegistry());
                                }
                            });
                            ((CitizensAttachmentFactory) implementation.getAttachmentFactory()).addPlotters(metrics);
                            metrics.start();
                            Messaging.log("Metrics started.");
                        } catch (IOException e) {
                            Messaging.logF("Unable to start metrics: %s.", e.getMessage());
                        }
                    }
                }.start();
            }
        }) == -1) {
            Messaging.severe("Issue enabling plugin. Disabling.");
            getServer().getPluginManager().disablePlugin(this);
        }
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
        ScriptCompiler compiler = CitizensAPI.getScriptCompiler();
        compiler.registerGlobalContextProvider(new EventRegistrar(this));
        compiler.registerGlobalContextProvider(new ObjectProvider("plugin", this));
    }

    public void reload() throws NPCLoadException {
        Editor.leaveAll();
        config.reload();
        despawnNPCs();
        setupNPCs();

        CitizensAPI.getServer().callEvent(new CitizensReloadEvent());
    }

    private void despawnNPCs() {
        Iterator<NPC> itr = CitizensAPI.getNPCRegistry().iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            itr.remove();
            npc.despawn();
        }
    }

    public void save() {
        for (NPC npc : CitizensAPI.getNPCRegistry())
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
            MobType type = MobType.fromName(unparsedEntityType);
            if (type == null) {
                try {
                    type = MobType.valueOf(unparsedEntityType);
                } catch (IllegalArgumentException ex) {
                    Messaging.logF("NPC type '%s' was not recognized. Did you spell it correctly?", unparsedEntityType);
                    continue;
                }
            }
            CitizensNPC npc = new CitizensNPC(key.getString("name"));
            npc.setEntityController(null);// TODO
            npc.load(key);

            ++created;
            if (npc.isSpawned())
                ++spawned;
        }
        Messaging.logF("Loaded %d NPCs (%d spawned).", created, spawned);
    }

    private void setupClassLoader() {
        Loader loader = new Loader(getClassLoader(), ClassPool.getDefault());
        try {
            loader.addTranslator(ClassPool.getDefault(), new BukkitEventConverter());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
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

    private boolean suggestClosestModifier(org.bukkit.command.CommandSender sender, String command, String modifier) {
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