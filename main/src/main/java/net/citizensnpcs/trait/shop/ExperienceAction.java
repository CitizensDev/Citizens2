package net.citizensnpcs.trait.shop;

import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.gui.InputMenus;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.util.Util;

public class ExperienceAction extends NPCShopAction {
    @Persist
    public int exp;

    public ExperienceAction() {
    }

    public ExperienceAction(int cost) {
        exp = cost;
    }

    @Override
    public String describe() {
        return exp == 1 ? exp + " level" : exp + " levels";
    }

    @Override
    public int getMaxRepeats(Entity entity, ItemStack[] inventory) {
        if (!(entity instanceof Player))
            return 0;

        return ((Player) entity).getLevel() / exp;
    }

    @Override
    public Transaction grant(Entity entity, ItemStack[] inventory, int repeats) {
        if (!(entity instanceof Player))
            return Transaction.fail();

        Player player = (Player) entity;
        int amount = exp * repeats;
        return Transaction.create(() -> true, () -> {
            player.setLevel(player.getLevel() + amount);
        }, () -> {
            player.setLevel(player.getLevel() - amount);
        });
    }

    @Override
    public Transaction take(Entity entity, ItemStack[] inventory, int repeats) {
        if (!(entity instanceof Player))
            return Transaction.fail();

        Player player = (Player) entity;
        int amount = exp * repeats;
        return Transaction.create(() -> (player.getLevel() >= amount), () -> {
            player.setLevel(player.getLevel() - amount);
        }, () -> {
            player.setLevel(player.getLevel() + amount);
        });
    }

    public static class ExperienceActionGUI implements GUI {
        @Override
        public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback) {
            ExperienceAction action = previous == null ? new ExperienceAction() : (ExperienceAction) previous;
            return InputMenus.filteredStringSetter(() -> Integer.toString(action.exp), s -> {
                try {
                    int result = Integer.parseInt(s);
                    if (result < 0)
                        return false;

                    action.exp = result;
                } catch (NumberFormatException nfe) {
                    return false;
                }
                callback.accept(action);
                return true;
            });
        }

        @Override
        public ItemStack createMenuItem(NPCShopAction previous) {
            String description = null;
            if (previous != null) {
                ExperienceAction old = (ExperienceAction) previous;
                description = old.describe();
            }
            return Util.createItem(Material.EXPERIENCE_BOTTLE, "XP Level", description);
        }

        @Override
        public boolean manages(NPCShopAction action) {
            return action instanceof ExperienceAction;
        }
    }
}