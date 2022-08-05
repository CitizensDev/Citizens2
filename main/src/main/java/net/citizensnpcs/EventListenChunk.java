package net.citizensnpcs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;

public class EventListenChunk implements Listener {
    EventListen listen;

    EventListenChunk(EventListen listen) {
        this.listen = listen;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        listen.loadNPCs(event);
    }
}
