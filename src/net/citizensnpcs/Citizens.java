package net.citizensnpcs;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.npc.NPCManager;
import net.citizensnpcs.npc.CitizensNPCManager;

import org.bukkit.plugin.java.JavaPlugin;

public class Citizens extends JavaPlugin implements CitizensPlugin {

	@Override
	public void onDisable() {
	}

	@Override
	public void onEnable() {
		CitizensAPI.setInstance(this);
	}

	@Override
	public NPCManager getNPCManager() {
		return new CitizensNPCManager();
	}
}