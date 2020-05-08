package net.citizensnpcs.api.event;

import java.util.List;

import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;

public class NPCDeathEvent extends NPCEvent {
    private final EntityDeathEvent event;

    public NPCDeathEvent(NPC npc, EntityDeathEvent event) {
        super(npc);
        this.event = event;
    }

    public int getDroppedExp() {
        return event.getDroppedExp();
    }

    public List<ItemStack> getDrops() {
        return event.getDrops();
    }

    public EntityDeathEvent getEvent() {
        return event;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public void setDroppedExp(int exp) {
        event.setDroppedExp(exp);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}