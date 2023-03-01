package net.citizensnpcs.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

/**
 * Called when an NPC is right-clicked by a player.
 */
public class NPCRightClickEvent extends NPCClickEvent {
    private boolean toCancel;

    public NPCRightClickEvent(NPC npc, Player rightClicker) {
        super(npc, rightClicker);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public boolean isDelayedCancellation() {
        return toCancel;
    }

    public void setDelayedCancellation(boolean toCancel) {
        this.toCancel = true;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}