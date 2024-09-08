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
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.InventoryMultiplexer;
import net.citizensnpcs.util.Util;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

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
        return economy.format(money);
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
            EconomyResponse response = economy.depositPlayer(player, amount);
            if (response != null
                    && (response.type == ResponseType.FAILURE || response.type == ResponseType.NOT_IMPLEMENTED)) {
                Messaging.severe("Failed to deposit", amount, "to", player, "in NPC shop:", response.errorMessage);
            }
        }, () -> {
            EconomyResponse response = economy.withdrawPlayer(player, amount);
            if (response != null
                    && (response.type == ResponseType.FAILURE || response.type == ResponseType.NOT_IMPLEMENTED)) {
                Messaging.severe("Failed to withdraw", amount, "from", player, "in NPC shop:", response.errorMessage);
            }
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
            EconomyResponse response = economy.withdrawPlayer(player, amount);
            if (response != null
                    && (response.type == ResponseType.FAILURE || response.type == ResponseType.NOT_IMPLEMENTED)) {
                Messaging.severe("Failed to withdraw", amount, "from", player, "in NPC shop:", response.errorMessage);
            }
        }, () -> {
            EconomyResponse response = economy.depositPlayer(player, amount);
            if (response != null
                    && (response.type == ResponseType.FAILURE || response.type == ResponseType.NOT_IMPLEMENTED)) {
                Messaging.severe("Failed to deposit", amount, "to", player, "in NPC shop:", response.errorMessage);
            }
        });
    }

    public static class MoneyActionGUI implements GUI {
        private Boolean supported;

        public MoneyActionGUI() {
            try {
                Class.forName("net.milkbowl.vault.economy.Economy");
            } catch (ClassNotFoundException e) {
                supported = false;
            }
        }

        @Override
        public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback) {
            MoneyAction action = previous == null ? new MoneyAction() : (MoneyAction) previous;
            return InputMenus.filteredStringSetter(() -> Double.toString(action.money), s -> {
                try {
                    double result = Double.parseDouble(Messaging.stripColor(s));
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
                    supported = Bukkit.getServicesManager().getRegistration(Economy.class) != null
                            && Bukkit.getServicesManager().getRegistration(Economy.class).getProvider() != null;
                } catch (Throwable t) {
                    supported = false;
                    Messaging.severe("Error fetching shop economy provider, shop economy integration will not work:");
                    t.printStackTrace();
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