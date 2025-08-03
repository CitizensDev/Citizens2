package net.citizensnpcs.api.event;

import org.bukkit.event.HandlerList;
import org.bukkit.inventory.meta.ItemMeta;

import net.citizensnpcs.api.util.DataKey;

public class CitizensSerialiseMetaEvent extends CitizensEvent {
    private final DataKey key;
    private final ItemMeta meta;

    public CitizensSerialiseMetaEvent(DataKey key, ItemMeta meta) {
        this.key = key;
        this.meta = meta;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public DataKey getKey() {
        return key;
    }

    public ItemMeta getMeta() {
        return meta;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();
}
