package net.citizensnpcs.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class WorldListen implements Listener {

	@EventHandler(event = ChunkLoadEvent.class, priority = EventPriority.NORMAL)
	public void onChunkLoad(ChunkLoadEvent event) {

	}

	@EventHandler(event = ChunkLoadEvent.class, priority = EventPriority.NORMAL)
	public void onChunkUnload(ChunkUnloadEvent event) {

	}
}