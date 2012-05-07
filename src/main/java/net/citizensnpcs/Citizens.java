package net.citizensnpcs;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CitizensReloadEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.scripting.EventRegistrar;
import net.citizensnpcs.api.scripting.ObjectProvider;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.trait.TraitManager;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.DatabaseStorage;
import net.citizensnpcs.api.util.NBTStorage;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.api.npc.character.Character;
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
import net.citizensnpcs.npc.CitizensCharacterManager;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.CitizensTraitManager;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.Metrics;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Iterators;

public class Citizens extends JavaPlugin {
    private final CitizensCharacterManager characterManager = new CitizensCharacterManager();
    private final CommandManager commands = new CommandManager();
    private boolean compatible;
    private Settings config;
    private ClassLoader contextClassLoader;
    private CitizensNPCManager npcManager;
    private Storage saves;
    private TraitManager traitManager;

    public CommandManager getCommandManager() {
        return commands;
    }

    public CitizensNPCManager getNPCManager() {
        return npcManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;

        try {
            // must put command into split.
            String[] split = new String[args.length + 1];
            System.arraycopy(args, 0, split, 1, args.length);
            split[0] = cmd.getName().toLowerCase();

            String modifier = args.length > 0 ? args[0] : "";

            if (!commands.hasCommand(split[0], modifier) && !modifier.isEmpty()) {
                return suggestClosestModifier(sender, split[0], modifier);
            }

            NPC npc = null;
            if (player != null && player.getMetadata("selected").size() > 0)
                npc = npcManager.getNPC(player.getMetadata("selected").get(0).asInt());
            // TODO: change the args supplied to a context style system for
            // flexibility (ie. adding more context in the future without
            // changing everything)
            try {
                commands.execute(split, player, player == null ? sender : player, npc);
            } catch (ServerCommandException ex) {
                Messaging.send(sender, "You must be in-game to execute that command.");
            } catch (CommandUsageException ex) {
                Messaging.sendError(player, ex.getMessage());
                Messaging.sendError(player, ex.getUsage());
            } catch (WrappedCommandException ex) {
                throw ex.getCause();
            } catch (UnhandledCommandException ex) {
                return false;
            } catch (CommandException ex) {
                Messaging.sendError(sender, ex.getMessage());
            }
        } catch (NumberFormatException ex) {
            Messaging.sendError(player, "That is not a valid number.");
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (sender instanceof Player) {
                Messaging.sendError(player, "Please report this error: [See console]");
                Messaging.sendError(player, ex.getClass().getName() + ": " + ex.getMessage());
            }
        }
        return true;
    }

    @Override
    public void onDisable() {
        tearDownScripting();
        // Don't bother with this part if MC versions are not compatible
        if (compatible) {
            save();
            npcManager.safeRemove();
            npcManager = null;
            getServer().getScheduler().cancelTasks(this);
        }

        Messaging.log("v" + getDescription().getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        // Disable if the server is not using the compatible Minecraft version
        String mcVersion = ((CraftServer) getServer()).getServer().getVersion();
        compatible = mcVersion.startsWith(COMPATIBLE_MC_VERSION);
        if (!compatible) {
            Messaging.log(Level.SEVERE, "v" + getDescription().getVersion() + " is not compatible with Minecraft v"
                    + mcVersion + ". Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        registerScriptHelpers();

        config = new Settings(getDataFolder());

        setupStorage();

        npcManager = new CitizensNPCManager(this, saves);
        traitManager = new CitizensTraitManager(this);
        CitizensAPI.setNPCManager(npcManager);
        CitizensAPI.setCharacterManager(characterManager);
        CitizensAPI.setTraitManager(traitManager);
        CitizensAPI.setDataFolder(getDataFolder());

        getServer().getPluginManager().registerEvents(new EventListen(npcManager), this);

        registerCommands();

        Messaging.log("v" + getDescription().getVersion() + " enabled.");

        // Setup NPCs after all plugins have been enabled (allows for multiworld
        // support and for NPCs to properly register external settings)
        if (getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                setupNPCs(); 
                // Run metrics "last"
                startMetrics();
            }
        }) == -1) {
            Messaging.log(Level.SEVERE, "Issue enabling plugin. Disabling.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void startMetrics() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Messaging.log("Starting Metrics");
                    Metrics metrics = new Metrics(Citizens.this);
                    metrics.addCustomData(new Metrics.Plotter("Total NPCs") {
                        @Override
                        public int getValue() {
                            return Iterators.size(npcManager.iterator());
                        }
                    });
                    Metrics.Graph graph = metrics.createGraph("Character Type Usage");
                    for(final Character character : characterManager.getRegistered()){            
                        graph.addPlotter(new Metrics.Plotter(StringHelper.capitalize(character.getName())) {
                            @Override
                            public int getValue() {
                                return npcManager.getNPCs(character.getClass()).size();
                            }
                        });
                    }
                    metrics.start();
                } catch (IOException ex) {
                    Messaging.log("Unable to load metrics");
                }
            }
        }.start();
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
        Messaging.log("Save method set to", saves.toString());
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
        npcManager.safeRemove();
        setupNPCs();

        getServer().getPluginManager().callEvent(new CitizensReloadEvent());
    }

    private void setupScripting() {
        contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClassLoader());
        // workaround to fix scripts not loading plugin classes properly
    }

    private void tearDownScripting() {
        Thread.currentThread().setContextClassLoader(contextClassLoader);
    }

    public void save() {
        for (NPC npc : npcManager)
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
                Messaging.log("Could not find a name for the NPC with ID '" + id + "'.");
                continue;
            }
            String unparsedEntityType = key.getString("traits.type", "PLAYER");
            EntityType type = EntityType.fromName(unparsedEntityType);
            if (type == null) {
                try {
                    type = EntityType.valueOf(unparsedEntityType);
                } catch (IllegalArgumentException ex) {
                    Messaging.log("NPC type '" + unparsedEntityType
                            + "' was not recognized. Did you spell it correctly?");
                    continue;
                }
            }
            NPC npc = npcManager.createNPC(type, id, key.getString("name"), null);
            ((CitizensNPC) npc).load(key);

            ++created;
            if (npc.isSpawned())
                ++spawned;
        }
        Messaging.log("Loaded " + created + " NPCs (" + spawned + " spawned).");
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

    private static final String COMPATIBLE_MC_VERSION = "1.2.5";
}