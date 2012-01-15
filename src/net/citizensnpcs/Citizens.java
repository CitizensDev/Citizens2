package net.citizensnpcs;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.npc.CitizensNPCManager;

import org.bukkit.plugin.java.JavaPlugin;

public class Citizens extends JavaPlugin {

	@Override
	public void onDisable() {
	}

	@Override
	public void onEnable() {
		CitizensAPI.setNPCManager(new CitizensNPCManager());
	}
}