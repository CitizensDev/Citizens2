package net.citizensnpcs.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.util.DataKey;

public class CitizensDeserialiseMetaEvent extends Event {
    private final ItemStack itemstack;
    private final DataKey key;

    public CitizensDeserialiseMetaEvent(DataKey key, ItemStack itemstack) {
        this.key = key;
        this.itemstack = itemstack;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public ItemStack getItemStack() {
        return itemstack;
    }

    public DataKey getKey() {
        return key;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
