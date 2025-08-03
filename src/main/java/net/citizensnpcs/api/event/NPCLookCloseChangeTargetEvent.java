package net.citizensnpcs.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;

public class NPCLookCloseChangeTargetEvent extends NPCEvent {
    private Player next;
    private final Player old;

    public NPCLookCloseChangeTargetEvent(NPC npc, Player old, Player next) {
        super(npc);
        this.old = old;
        this.next = next;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getNewTarget() {
        return next;
    }

    public Player getPreviousTarget() {
        return old;
    }

    public void setNewTarget(Player target) {
        this.next = target;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
