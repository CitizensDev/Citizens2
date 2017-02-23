package net.citizensnpcs.api.event;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Called when an NPC is left-clicked by a player.
 */
public class NPCLeftClickEvent extends NPCClickEvent {
    public NPCLeftClickEvent(NPC npc, Player leftClicker) {
        super(npc, leftClicker);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}