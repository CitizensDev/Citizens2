package net.citizensnpcs.api.event;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

public class CitizensListener extends CustomEventListener {

	@Override
	public void onCustomEvent(Event event) {
		if (event instanceof NPCSpawnEvent) {
			onCitizensReload((CitizensReloadEvent) event);
		}
	}

	/**
	 * Called when Citizens is reloaded
	 */
	public void onCitizensReload(CitizensReloadEvent event) {
	}
}