package net.citizensnpcs.trait.shop;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.gui.InputMenus;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.util.Util;
import net.milkbowl.vault.economy.Economy;

public class MoneyAction extends NPCShopAction {
    @Persist
    public double money;

    public MoneyAction() {
    }

    @Override
    public Transaction grant(Entity entity) {
        if (!(entity instanceof Player))
            return Transaction.fail();
        Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        Player player = (Player) entity;
        return Transaction.create(() -> {
            return true;
        }, () -> {
            economy.depositPlayer(player, money);
        }, () -> {
            economy.withdrawPlayer(player, money);
        });
    }

    @Override
    public Transaction take(Entity entity) {
        if (!(entity instanceof Player))
            return Transaction.fail();
        Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        Player player = (Player) entity;
        return Transaction.create(() -> {
            return economy.has(player, money);
        }, () -> {
            economy.withdrawPlayer(player, money);
        }, () -> {
            economy.depositPlayer(player, money);
        });
    }

    public static class MoneyActionGUI implements GUI {
        private Boolean supported;

        @Override
        public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback) {
            final MoneyAction action = previous == null ? new MoneyAction() : (MoneyAction) previous;
            return InputMenus.filteredStringSetter(() -> Double.toString(action.money), (s) -> {
                try {
                    double result = Double.parseDouble(s);
                    if (result < 0)
                        return false;
                    action.money = result;
                } catch (NumberFormatException nfe) {
                    return false;
                }
                callback.accept(action);
                return true;
            });
        }

        @Override
        public ItemStack createMenuItem(NPCShopAction previous) {
            if (supported == null) {
                try {
                    supported = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider() != null;
                } catch (Throwable t) {
                    supported = false;
                }
            }
            if (!supported) {
                return null;
            }
            String description = null;
            if (previous != null) {
                Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
                MoneyAction old = (MoneyAction) previous;
                description = old.money + " " + economy.currencyNamePlural();
            }
            return Util.createItem(Material.GOLD_INGOT, "Money", description);
        }

        @Override
        public boolean manages(NPCShopAction action) {
            return action instanceof MoneyAction;
        }
    }
}