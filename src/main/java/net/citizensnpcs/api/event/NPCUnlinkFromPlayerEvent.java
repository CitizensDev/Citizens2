package net.citizensnpcs.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

public class NPCUnlinkFromPlayerEvent extends NPCEvent {
    private final Player player;

    public NPCUnlinkFromPlayerEvent(NPC npc, Player player) {
        super(npc);
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
