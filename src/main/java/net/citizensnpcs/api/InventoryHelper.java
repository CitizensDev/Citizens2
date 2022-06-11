package net.citizensnpcs.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

public interface InventoryHelper {
    void updateInventoryTitle(Player player, InventoryView view, String newTitle);
}
