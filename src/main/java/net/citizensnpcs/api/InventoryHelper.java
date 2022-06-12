package net.citizensnpcs.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

public interface InventoryHelper {
    InventoryView openAnvilInventory(Player player, Inventory inventory, String title);

    void updateInventoryTitle(Player player, InventoryView view, String newTitle);
}
