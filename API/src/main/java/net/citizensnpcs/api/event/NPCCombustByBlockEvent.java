package net.citizensnpcs.api.event;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityCombustByBlockEvent;

import net.citizensnpcs.api.npc.NPC;

public class NPCCombustByBlockEvent extends NPCCombustEvent {
    private final EntityCombustByBlockEvent event;

    public NPCCombustByBlockEvent(EntityCombustByBlockEvent event, NPC npc) {
        super(event, npc);
        this.event = event;
    }

    /**
     * The combuster can be lava or a block that is on fire.
     * <p>
     * WARNING: block may be null.
     *
     * @return the Block that set the combustee alight.
     */
    public Block getCombuster() {
        return event.getCombuster();
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
