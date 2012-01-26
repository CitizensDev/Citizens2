package net.citizensnpcs;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.DefaultInstanceFactory;
import net.citizensnpcs.api.npc.trait.InstanceFactory;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.trait.Owner;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.command.CommandManager;
import net.citizensnpcs.command.Injector;
import net.citizensnpcs.command.command.NPCCommands;
import net.citizensnpcs.command.exception.CommandUsageException;
import net.citizensnpcs.command.exception.MissingNestedCommandException;
import net.citizensnpcs.command.exception.NoPermissionsException;
import net.citizensnpcs.command.exception.RequirementMissingException;
import net.citizensnpcs.command.exception.ServerCommandException;
import net.citizensnpcs.command.exception.UnhandledCommandException;
import net.citizensnpcs.command.exception.WrappedCommandException;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.storage.Storage;
import net.citizensnpcs.storage.database.DatabaseStorage;
import net.citizensnpcs.storage.flatfile.YamlStorage;
import net.citizensnpcs.util.ByIdArray;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

public class Citizens extends JavaPlugin {
    private static Storage saves;

    private CitizensNPCManager npcManager;
    private final InstanceFactory<Character> characterManager = new DefaultInstanceFactory<Character>();
    private final InstanceFactory<Trait> traitManager = new DefaultInstanceFactory<Trait>();
    private CommandManager cmdManager;
    private Settings config;

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

            String modifier = "";
            if (args.length > 0)
                modifier = args[0];

            // No command found!
            if (!cmdManager.hasCommand(split[0], modifier)) {
                if (!modifier.isEmpty()) {
                    boolean value = handleMistake(sender, split[0], modifier);
                    return value;
                }
            }

            NPC npc = null;
            if (player != null && npcManager.hasNPCSelected(player))
                npc = npcManager.getSelectedNPC(player);

            try {
                cmdManager.execute(split, player, player == null ? sender : player, npc);
            } catch (ServerCommandException ex) {
                sender.sendMessage("You must be in-game to execute that command.");
            } catch (NoPermissionsException ex) {
                Messaging.sendError(player, "You don't have permission to execute that command.");
            } catch (MissingNestedCommandException ex) {
                Messaging.sendError(player, ex.getUsage());
            } catch (CommandUsageException ex) {
                Messaging.sendError(player, ex.getMessage());
                Messaging.sendError(player, ex.getUsage());
            } catch (RequirementMissingException ex) {
                Messaging.sendError(player, ex.getMessage());
            } catch (WrappedCommandException e) {
                throw e.getCause();
            } catch (UnhandledCommandException e) {
                return false;
            }
        } catch (NumberFormatException e) {
            Messaging.sendError(player, "That is not a valid number.");
        } catch (Throwable excp) {
            excp.printStackTrace();
            Messaging.sendError(player, "Please report this error: [See console]");
            Messaging.sendError(player, excp.getClass().getName() + ": " + excp.getMessage());
        }
        return true;
    }

    @Override
    public void onDisable() {
        // Save and despawn all NPCs
        config.save();
        saveNPCs();
        for (NPC npc : npcManager.getAllNPCs())
            npc.despawn();

        Bukkit.getScheduler().cancelTasks(this);

        Messaging.log("v" + getDescription().getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        // Configuration file
        config = new Settings(this);
        config.load();

        // NPC storage
        if (Setting.USE_DATABASE.getBoolean())
            saves = new DatabaseStorage();
        else
            saves = new YamlStorage(getDataFolder() + File.separator + "saves.yml");

        // Register API managers
        npcManager = new CitizensNPCManager(saves);
        CitizensAPI.setNPCManager(npcManager);
        CitizensAPI.setCharacterManager(characterManager);
        CitizensAPI.setTraitManager(traitManager);

        // Register events
        getServer().getPluginManager().registerEvents(new EventListen(npcManager), this);

        // Register commands and permissions
        registerCommands();
        registerPermissions();

        Messaging.log("v" + getDescription().getVersion() + " enabled.");

        // Setup NPCs after all plugins have been enabled (allows for multiworld
        // support and for NPCs to properly register external settings)
        if (Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    setupNPCs();
                } catch (NPCLoadException ex) {
                    Messaging.log("Issue when loading NPCs: " + ex.getMessage());
                }
            }
        }) == -1) {
            Messaging.log("Issue enabling plugin. Disabling.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public static Storage getNPCStorage() {
        return saves;
    }

    private void saveNPCs() {
        for (NPC npc : npcManager.getAllNPCs())
            ((CitizensNPC) npc).save();
        getNPCStorage().save();
    }

    private void setupNPCs() throws NPCLoadException {
        traitManager.register("location", SpawnLocation.class);
        traitManager.register("owner", Owner.class);

        for (DataKey key : getNPCStorage().getKey("npc").getIntegerSubKeys()) {
            int id = Integer.parseInt(key.name());
            if (!key.keyExists("name"))
                throw new NPCLoadException("Could not find a name for the NPC with ID '" + id + "'.");
            Character character = characterManager.getInstance(key.getString("character"));
            NPC npc = npcManager.createNPC(id, key.getString("name"), character);

            // Load the character if it exists, otherwise remove the character
            if (character != null)
                character.load(key.getRelative("characters." + character.getName()));
            else {
                if (key.keyExists("character")) {
                    Messaging.debug("Character '" + key.getString("character")
                            + "' does not exist. Removing character from the NPC with ID '" + npc.getId() + "'.");
                    key.removeKey("character");
                }
            }

            // Load traits
            for (DataKey traitKey : key.getSubKeys()) {
                Trait trait = traitManager.getInstance(traitKey.name());
                if (trait == null)
                    continue;
                trait.load(traitKey);
                npc.addTrait(trait);
            }

            // Spawn the NPC
            if (key.getBoolean("spawned"))
                npc.spawn(npc.getTrait(SpawnLocation.class).getLocation());
        }
        Messaging.log("Loaded " + ((ByIdArray<NPC>) npcManager.getAllNPCs()).size() + " NPCs ("
                + ((ByIdArray<NPC>) npcManager.getSpawnedNPCs()).size() + " spawned).");
    }

    private void registerPermissions() {
        Map<String, Boolean> children = new HashMap<String, Boolean>();
        children.put("citizens.npc.create", true);
        children.put("citizens.npc.spawn", true);
        children.put("citizens.npc.despawn", true);
        children.put("citizens.npc.select", true);

        Permission perm = new Permission("citizens.*", PermissionDefault.OP, children);
        getServer().getPluginManager().addPermission(perm);
    }

    private void registerCommands() {
        cmdManager = new CommandManager(npcManager);
        cmdManager.setInjector(new Injector(npcManager, characterManager));

        // cmdManager.register(AdminCommands.class);
        cmdManager.register(NPCCommands.class);
    }

    private boolean handleMistake(CommandSender sender, String command, String modifier) {
        String[] modifiers = cmdManager.getAllCommandModifiers(command);
        Map<Integer, String> values = new TreeMap<Integer, String>();
        int i = 0;
        for (String string : modifiers) {
            values.put(StringHelper.getLevenshteinDistance(modifier, string), modifiers[i]);
            ++i;
        }
        int best = 0;
        boolean stop = false;
        Set<String> possible = new HashSet<String>();
        for (Entry<Integer, String> entry : values.entrySet()) {
            if (!stop) {
                best = entry.getKey();
                stop = true;
            } else if (entry.getKey() > best)
                break;
            possible.add(entry.getValue());
        }
        if (possible.size() > 0) {
            sender.sendMessage(ChatColor.GRAY + "Unknown command. Did you mean:");
            for (String string : possible)
                sender.sendMessage(StringHelper.wrap(" /") + command + " " + StringHelper.wrap(string));
            return true;
        }
        return false;
    }
}