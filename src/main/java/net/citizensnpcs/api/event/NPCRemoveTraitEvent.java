package net.citizensnpcs.api.event;

import org.bukkit.event.HandlerList;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;

public class NPCRemoveTraitEvent extends NPCTraitEvent {
    public NPCRemoveTraitEvent(NPC npc, Trait trait) {
        super(npc, trait);
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
