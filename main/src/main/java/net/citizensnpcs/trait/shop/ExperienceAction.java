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
        this.exp = cost;
    }

    @Override
    public String describe() {
        return exp + " XP";
    }

    @Override
    public Transaction grant(Entity entity) {
        if (!(entity instanceof Player))
            return Transaction.fail();
        Player player = (Player) entity;
        return Transaction.create(() -> {
            return true;
        }, () -> {
            player.setLevel(player.getLevel() + exp);
        }, () -> {
            player.setLevel(player.getLevel() - exp);
        });
    }

    @Override
    public Transaction take(Entity entity) {
        if (!(entity instanceof Player))
            return Transaction.fail();
        Player player = (Player) entity;
        return Transaction.create(() -> {
            return player.getLevel() >= exp;
        }, () -> {
            player.setLevel(player.getLevel() - exp);
        }, () -> {
            player.setLevel(player.getLevel() + exp);
        });
    }

    public static class ExperienceActionGUI implements GUI {
        @Override
        public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback) {
            final ExperienceAction action = previous == null ? new ExperienceAction() : (ExperienceAction) previous;
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
            return Util.createItem(Material.EXPERIENCE_BOTTLE, "Experience", description);
        }

        @Override
        public boolean manages(NPCShopAction action) {
            return action instanceof ExperienceAction;
        }
    }
}