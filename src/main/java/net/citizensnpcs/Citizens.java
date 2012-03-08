package net.citizensnpcs;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CitizensReloadEvent;
import net.citizensnpcs.api.exception.NPCException;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.DatabaseStorage;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.command.CommandManager;
import net.citizensnpcs.command.Injector;
import net.citizensnpcs.command.command.AdminCommands;
import net.citizensnpcs.command.command.EditorCommands;
import net.citizensnpcs.command.command.HelpCommands;
import net.citizensnpcs.command.command.NPCCommands;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.command.exception.CommandUsageException;
import net.citizensnpcs.command.exception.ServerCommandException;
import net.citizensnpcs.command.exception.UnhandledCommandException;
import net.citizensnpcs.command.exception.WrappedCommandException;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.CitizensCharacterManager;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.CitizensTraitManager;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.Metrics;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Iterators;

public class Citizens extends JavaPlugin {
    private static final String COMPATIBLE_MC_VERSION = "1.2.3";

    private final CommandManager commands = new CommandManager();
    private boolean compatible;
    private Settings config;
    private CitizensCharacterManager characterManager;
    private volatile CitizensNPCManager npcManager;
    private Storage saves;

    public CitizensCharacterManager getCharacterManager() {
        return characterManager;
    }

    public CommandManager getCommandManager() {
        return commands;
    }

    public CitizensNPCManager getNPCManager() {
        return npcManager;
    }

    public Storage getStorage() {
        return saves;
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
                Messaging.sendError(player, ex.getMessage());
            }
        } catch (NumberFormatException ex) {
            Messaging.sendError(player, "That is not a valid number.");
        } catch (Throwable ex) {
            ex.printStackTrace();
            Messaging.sendError(player, "Please report this error: [See console]");
            Messaging.sendError(player, ex.getClass().getName() + ": " + ex.getMessage());
        }
        return true;
    }

    @Override
    public void onDisable() {
        // Don't bother with this part if MC versions are not compatible
        if (compatible) {
            save();
            for (NPC npc : npcManager)
                npc.despawn();
            getServer().getScheduler().cancelTasks(this);
        }

        Messaging.log("v" + getDescription().getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        // Disable if the server is not using the compatible Minecraft version
        compatible = ((CraftServer) getServer()).getServer().getVersion().startsWith(COMPATIBLE_MC_VERSION);
        String mcVersion = ((CraftServer) getServer()).getServer().getVersion();
        if (!compatible) {
            Messaging.log(Level.SEVERE, "v" + getDescription().getVersion() + " is not compatible with Minecraft v"
                    + mcVersion + ". Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Configuration file
        config = new Settings(this);
        config.load();

        // NPC storage
        if (Setting.USE_DATABASE.asBoolean()) {
            try {
                saves = new DatabaseStorage(Setting.DATABASE_DRIVER.asString(), Setting.DATABASE_URL.asString(),
                        Setting.DATABASE_USERNAME.asString(), Setting.DATABASE_PASSWORD.asString());
            } catch (SQLException e) {
                Messaging.log("Unable to connect to database, falling back to YAML");
                saves = new YamlStorage(getDataFolder() + File.separator + "saves.yml", "Citizens NPC Storage");
            }
        } else {
            saves = new YamlStorage(getDataFolder() + File.separator + "saves.yml", "Citizens NPC Storage");
        }

        // Register API managers
        npcManager = new CitizensNPCManager(saves);
        characterManager = new CitizensCharacterManager();
        CitizensAPI.setNPCManager(npcManager);
        CitizensAPI.setCharacterManager(characterManager);
        CitizensAPI.setTraitManager(new CitizensTraitManager());

        // Register events
        getServer().getPluginManager().registerEvents(new EventListen(npcManager), this);

        // Register commands
        registerCommands();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new NPCUpdater(npcManager), 0, 1);

        Messaging.log("v" + getDescription().getVersion() + " enabled.");

        // Setup NPCs after all plugins have been enabled (allows for multiworld
        // support and for NPCs to properly register external settings)
        if (Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    setupNPCs();
                } catch (NPCLoadException ex) {
                    Messaging.log(Level.SEVERE, "Issue when loading NPCs: " + ex.getMessage());
                }
            }
        }) == -1) {
            Messaging.log(Level.SEVERE, "Issue enabling plugin. Disabling.");
            getServer().getPluginManager().disablePlugin(this);
        }

        // Run metrics last
        new Thread() {
            @Override
            public void run() {
                try {
                    Metrics metrics = new Metrics();
                    metrics.addCustomData(Citizens.this, new Metrics.Plotter() {
                        @Override
                        public String getColumnName() {
                            return "Total NPCs";
                        }

                        @Override
                        public int getValue() {
                            return Iterators.size(npcManager.iterator());
                        }
                    });
                    metrics.beginMeasuringPlugin(Citizens.this);
                } catch (IOException ex) {
                    Messaging.log("Unable to load metrics");
                }
            }
        }.start();
    }

    private void registerCommands() {
        commands.setInjector(new Injector(this));

        // Register command classes
        commands.register(AdminCommands.class);
        commands.register(EditorCommands.class);
        commands.register(HelpCommands.class);
        commands.register(NPCCommands.class);
    }

    public void reload() throws NPCLoadException {
        Editor.leaveAll();
        config.load();
        for (NPC npc : npcManager)
            npc.despawn();

        saves.load();
        setupNPCs();

        getServer().getPluginManager().callEvent(new CitizensReloadEvent());
    }

    public void save() {
        config.save();
        for (NPC npc : npcManager)
            npc.save(saves.getKey("npc." + npc.getId()));
        saves.save();
    }

    private void setupNPCs() throws NPCLoadException {
        int created = 0, spawned = 0;
        for (DataKey key : saves.getKey("npc").getIntegerSubKeys()) {
            int id = Integer.parseInt(key.name());
            if (!key.keyExists("name"))
                throw new NPCLoadException("Could not find a name for the NPC with ID '" + id + "'.");

            NPC npc = npcManager.createNPC(EntityType.valueOf(key.getString("traits.type").toUpperCase()), id, key
                    .getString("name"), null);
            try {
                npc.load(key);
            } catch (NPCException ex) {
                Messaging.log(ex.getMessage());
            }

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
}