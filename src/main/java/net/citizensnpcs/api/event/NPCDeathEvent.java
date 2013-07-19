package net.citizensnpcs.api.event;

import java.util.List;

import net.citizensnpcs.api.npc.NPC;

import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

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

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public void setDroppedExp(int exp) {
        event.setDroppedExp(exp);
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }
}