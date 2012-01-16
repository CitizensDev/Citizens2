package net.citizensnpcs;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.npc.CitizensNPCManager;
import net.citizensnpcs.util.Messaging;

import org.bukkit.plugin.java.JavaPlugin;

public class Citizens extends JavaPlugin {

	@Override
	public void onDisable() {
		Messaging.log("v" + getDescription().getVersion() + " disabled.");
	}

	@Override
	public void onEnable() {
		CitizensAPI.setNPCManager(new CitizensNPCManager());

		// TODO wait to load until after all plugins using CitizensAPI are
		// loaded
		Messaging.log("v" + getDescription().getVersion() + " enabled.");
	}
}