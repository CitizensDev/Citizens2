package net.citizensnpcs.api;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.SkullMeta;

import net.citizensnpcs.api.util.SpigotUtil.InventoryViewAPI;

public interface NMSHelper {
    public OfflinePlayer getPlayer(BlockCommandSender sender);

    public String getTexture(SkullMeta meta);

    InventoryViewAPI openAnvilInventory(Player player, Inventory inventory, String title);

    public void setTexture(String string, SkullMeta meta);

    void updateInventoryTitle(Player player, InventoryViewAPI view, String newTitle);
}
