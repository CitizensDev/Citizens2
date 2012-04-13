package net.citizensnpcs.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC is selected by a player.
 */
public class NPCSelectEvent extends NPCEvent {
    private final Player player;

    public NPCSelectEvent(NPC npc, Player player) {
        super(npc);
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the player that selected an NPC.
     * 
     * @return Player that selected an NPC
     */
    public Player getPlayer() {
        return player;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}