package net.citizensnpcs;

import java.io.File;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.trait.SpawnLocation;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.trait.CitizensCharacterManager;
import net.citizensnpcs.npc.trait.CitizensTraitManager;
import net.citizensnpcs.storage.Storage;
import net.citizensnpcs.storage.flatfile.YamlStorage;
import net.citizensnpcs.util.ByIdArray;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Citizens extends JavaPlugin {
    private static final CitizensNPCManager npcManager = new CitizensNPCManager();
    private static final CitizensCharacterManager characterManager = new CitizensCharacterManager();
    private static final CitizensTraitManager traitManager = new CitizensTraitManager();
    private Settings config;
    private Storage saves;

    public Citizens() {
        // Register API managers
        CitizensAPI.setNPCManager(npcManager);
        CitizensAPI.setCharacterManager(characterManager);
        CitizensAPI.setTraitManager(traitManager);
    }

    @Override
    public void onDisable() {
        config.save();
        saveNPCs();
        Bukkit.getScheduler().cancelTasks(this);

        Messaging.log("v" + getDescription().getVersion() + " disabled.");
    }

    @Override
    public void onEnable() {
        // Configuration file
        config = new Settings(this);
        config.load();

        // TODO database support
        saves = new YamlStorage(getDataFolder() + File.separator + "saves.yml");

        // Register events
        getServer().getPluginManager().registerEvents(new EventListen(npcManager), this);

        Messaging.log("v" + getDescription().getVersion() + " enabled.");

        // Setup NPCs after all plugins have been enabled (allows for multiworld
        // support and for NPCs to properly register external settings)
        if (Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    setupNPCs();
                } catch (NPCLoadException ex) {
                    Messaging.log("Failed to create NPC: " + ex.getMessage());
                }
            }
        }) == -1) {
            Messaging.log("Issue enabling plugin. Disabling.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {
        if (args[0].equals("spawn")) {
            NPC npc = npcManager.createNPC("aPunch");
            npc.spawn(((Player) sender).getLocation());
        } else if (args[0].equals("despawn")) {
            for (NPC npc : npcManager.getSpawnedNPCs()) {
                npc.despawn();
            }
        }
        return true;
    }

    // TODO possibly separate this out some more
    private void setupNPCs() throws NPCLoadException {
        traitManager.registerTrait("location", SpawnLocation.class);
        for (DataKey key : saves.getKey("npc").getIntegerSubKeys()) {
            int id = Integer.parseInt(key.name());
            if (!key.keyExists("name"))
                throw new NPCLoadException("Could not find a name for the NPC with ID '" + id + "'.");
            Character character = characterManager.getCharacter(key.getString("character"));
            NPC npc = npcManager.createNPC(key.getString("name"), character);

            // Load the character if it exists, otherwise remove the character
            if (character != null) {
                character.load(key.getRelative(character.getName()));
            } else {
                if (key.keyExists("character")) {
                    Messaging.debug("Character '" + key.getString("character")
                            + "' does not exist. Removing character from the NPC with ID '" + npc.getId() + "'.");
                    key.removeKey("character");
                }
            }

            // Load traits
            for (DataKey traitKey : key.getSubKeys()) {
                Trait trait = traitManager.getTrait(traitKey.name());
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

    private void saveNPCs() {
        for (NPC npc : npcManager.getAllNPCs()) {
            DataKey root = saves.getKey("npc." + npc.getId());
            root.setString("name", npc.getFullName());
            root.setBoolean("spawned", npc.getBukkitEntity().isDead());

            // Save the character if it exists
            if (npc.getCharacter() != null) {
                root.setString("character", npc.getCharacter().getName());
                npc.getCharacter().save(root.getRelative(npc.getCharacter().getName()));
            }

            // Save all existing traits
            for (Trait trait : npc.getTraits())
                trait.save(root.getRelative(trait.getName()));
        }
        saves.save();
    }
}