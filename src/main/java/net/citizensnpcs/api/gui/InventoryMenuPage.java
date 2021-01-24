package net.citizensnpcs.api.gui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;

public abstract class InventoryMenuPage {
    public abstract void create(MenuContext ctx);

    public void onClick(InventoryMenuSlot slot, InventoryClickEvent event) {
    }

    public void onClose(HumanEntity player) {
    }
}
