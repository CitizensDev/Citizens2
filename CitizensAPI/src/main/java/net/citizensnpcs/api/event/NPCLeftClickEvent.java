package net.citizensnpcs.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

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

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}