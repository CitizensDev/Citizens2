package net.citizensnpcs.trait.shop;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.gui.InputMenus;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.trait.ShopTrait.NPCShop;
import net.citizensnpcs.util.InventoryMultiplexer;
import net.citizensnpcs.util.Util;

public class OpenShopAction extends NPCShopAction {
    @Persist
    public String shopName;

    public OpenShopAction() {
    }

    public OpenShopAction(String shopName) {
        this.shopName = shopName;
    }

    @Override
    public String describe() {
        NPCShop shop = ((Citizens) CitizensAPI.getPlugin()).getShops().getShop(shopName);
        String description = "Open " + shop.getName();
        return description;
    }

    @Override
    public int getMaxRepeats(Entity entity, InventoryMultiplexer inventory) {
        return -1;
    }

    @Override
    public Transaction grant(Entity entity, InventoryMultiplexer inventory, int repeats) {
        return take(entity, inventory, repeats);
    }

    @Override
    public Transaction take(Entity entity, InventoryMultiplexer inventory, int repeats) {
        if (!(entity instanceof Player))
            return Transaction.fail();
        NPCShop shop = ((Citizens) CitizensAPI.getPlugin()).getShops().getShop(shopName);
        Player player = (Player) entity;
        // TODO: support hierarchical shops? would need to call InventoryMenu#transition somehow
        return Transaction.create(() -> shop.canView(player), () -> {
            player.closeInventory();
            Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> shop.display(player));
        }, () -> {
            // TODO: closeInventory()? transitionBack()?
        });
    }

    public static class OpenShopActionGUI implements GUI {
        @Override
        public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback) {
            OpenShopAction action = previous == null ? new OpenShopAction() : (OpenShopAction) previous;
            return InputMenus.stringSetter(() -> action.shopName, s -> {
                if (s == null || s.isEmpty()) {
                    callback.accept(null);
                    return;
                }
                action.shopName = s;
                callback.accept(action);
            });
        }

        @Override
        public ItemStack createMenuItem(NPCShopAction previous) {
            String description = null;
            if (previous != null) {
                OpenShopAction old = (OpenShopAction) previous;
                description = old.describe();
            }
            return Util.createItem(Material.BOOKSHELF, "Open Shop", description);
        }

        @Override
        public boolean manages(NPCShopAction action) {
            return action instanceof OpenShopAction;
        }
    }
}