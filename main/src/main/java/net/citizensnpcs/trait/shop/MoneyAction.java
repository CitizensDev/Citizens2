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
import net.citizensnpcs.util.InventoryMultiplexer;
import net.citizensnpcs.util.Util;
import net.milkbowl.vault.economy.Economy;

public class MoneyAction extends NPCShopAction {
    @Persist
    public double money;

    public MoneyAction() {
    }

    public MoneyAction(double cost) {
        money = cost;
    }

    @Override
    public String describe() {
        Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        return money + " " + economy.currencyNamePlural();
    }

    @Override
    public int getMaxRepeats(Entity entity, InventoryMultiplexer inventory) {
        if (!(entity instanceof Player))
            return 0;

        Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        return (int) Math.floor(economy.getBalance((Player) entity) / money);
    }

    @Override
    public Transaction grant(Entity entity, InventoryMultiplexer inventory, int repeats) {
        if (!(entity instanceof Player))
            return Transaction.fail();

        Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        Player player = (Player) entity;
        double amount = money * repeats;

        return Transaction.create(() -> true, () -> {
            economy.depositPlayer(player, amount);
        }, () -> {
            economy.withdrawPlayer(player, amount);
        });
    }

    @Override
    public Transaction take(Entity entity, InventoryMultiplexer inventory, int repeats) {
        if (!(entity instanceof Player))
            return Transaction.fail();

        Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        Player player = (Player) entity;
        double amount = money * repeats;

        return Transaction.create(() -> economy.has(player, amount), () -> {
            economy.withdrawPlayer(player, amount);
        }, () -> {
            economy.depositPlayer(player, amount);
        });
    }

    public static class MoneyActionGUI implements GUI {
        private Boolean supported;

        @Override
        public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback) {
            MoneyAction action = previous == null ? new MoneyAction() : (MoneyAction) previous;
            return InputMenus.filteredStringSetter(() -> Double.toString(action.money), s -> {
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
            if (!supported)
                return null;

            String description = null;
            if (previous != null) {
                MoneyAction old = (MoneyAction) previous;
                description = old.describe();
            }
            return Util.createItem(Material.GOLD_INGOT, "Money", description);
        }

        @Override
        public boolean manages(NPCShopAction action) {
            return action instanceof MoneyAction;
        }
    }
}