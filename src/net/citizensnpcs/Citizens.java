package net.citizensnpcs;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.trait.trait.LocationTrait;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.util.Messaging;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Citizens extends JavaPlugin {
	private CitizensNPCManager npcManager;

	@Override
	public void onDisable() {
		Messaging.log("v" + getDescription().getVersion() + " disabled.");
	}

	@Override
	public void onEnable() {
		npcManager = new CitizensNPCManager();
		CitizensAPI.setNPCManager(npcManager);

		// Register events
		new EventListen(this);

		Messaging.log("v" + getDescription().getVersion() + " enabled.");

		// Setup NPCs after all plugins have been enabled (allows for multiworld
		// support and for NPCs to properly register external settings)
		if (Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				setupNPCs();
			}
		}, 100) == -1) {
			Messaging.log("Issue enabling plugin. Disabling.");
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	private void setupNPCs() {
		// TODO set up saving
		for (NPC npc : npcManager.getNPCs()) {
			npc.spawn(((LocationTrait) npc.getTrait("location")).getLocation());
		}
		Messaging.log("Loaded " + npcManager.getNPCs().size() + " NPCs.");
	}
}