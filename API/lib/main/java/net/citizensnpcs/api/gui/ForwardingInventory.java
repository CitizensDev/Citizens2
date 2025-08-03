package net.citizensnpcs.api.gui;

import org.bukkit.inventory.Inventory;

public interface ForwardingInventory {
    Inventory getWrapped();
}
