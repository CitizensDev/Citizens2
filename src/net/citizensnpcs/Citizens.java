package net.citizensnpcs;

import java.io.File;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.DataKey;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.Character;
import net.citizensnpcs.api.npc.trait.Trait;
import net.citizensnpcs.api.npc.trait.trait.LocationTrait;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.npc.trait.CitizensCharacterManager;
import net.citizensnpcs.storage.Storage;
import net.citizensnpcs.storage.flatfile.YamlStorage;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Citizens extends JavaPlugin {
	private CitizensNPCManager npcManager;
	private CitizensCharacterManager characterManager;
	private Storage saves;

	@Override
	public void onDisable() {
		Messaging.log("v" + getDescription().getVersion() + " disabled.");
	}

	@Override
	public void onEnable() {
		npcManager = new CitizensNPCManager();
		characterManager = new CitizensCharacterManager();
		CitizensAPI.setNPCManager(npcManager);
		CitizensAPI.setCharacterManager(characterManager);

		// TODO database support
		saves = new YamlStorage(getDataFolder() + File.separator + "saves.yml");

		// Register events
		new EventListen(this);

		Messaging.log("v" + getDescription().getVersion() + " enabled.");

		// Setup NPCs after all plugins have been enabled (allows for multiworld
		// support and for NPCs to properly register external settings)
		if (Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				try {
					setupNPCs();
				} catch (NPCLoadException ex) {
					ex.printStackTrace();
				}
			}
		}, /* TODO how long should delay be? */100) == -1) {
			Messaging.log("Issue enabling plugin. Disabling.");
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {
		if (args[0].equals("test")) {
			NPC npc = npcManager.createNPC("aPunch");
			npc.spawn(((Player) sender).getLocation());
		}
		return true;
	}

	private void setupNPCs() throws NPCLoadException {
		// TODO needs fixing
		for (DataKey key : saves.getKey("npc").getIntegerSubKeys()) {
			int id = Integer.parseInt(key.name());
			if (!key.keyExists("name"))
				throw new NPCLoadException("Could not find a name for the NPC with ID '" + id + "'.");
			Character character = characterManager.getCharacter(key.getString("character"));
			NPC npc = npcManager.createNPC(key.getString("name"), character);
			if (character != null) {
				character.load(key);
			}
			for (Trait t : npc.getTraits()) {
				t.load(key);
			}
			npc.spawn(npc.getTrait(LocationTrait.class).getLocation());
		}
		Messaging.log("Loaded " + npcManager.getNPCs().size() + " NPCs.");
	}
}