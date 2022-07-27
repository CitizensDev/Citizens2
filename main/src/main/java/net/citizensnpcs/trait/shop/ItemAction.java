package net.citizensnpcs.trait.shop;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.InventoryMenuSlot;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.util.Util;

public class ItemAction extends NPCShopAction {
    @Persist
    public List<ItemStack> items = Lists.newArrayList();

    public ItemAction() {
    }

    public ItemAction(ItemStack... items) {
        this(Arrays.asList(items));
    }

    public ItemAction(List<ItemStack> items) {
        this.items = items;
    }

    @Override
    public Transaction grant(Entity entity) {
        if (!(entity instanceof InventoryHolder))
            return Transaction.fail();
        Inventory source = ((InventoryHolder) entity).getInventory();
        return Transaction.create(() -> {
            int free = 0;
            for (ItemStack stack : source.getContents()) {
                if (stack == null || stack.getType() == Material.AIR) {
                    free++;
                    continue;
                }
            }
            return free >= items.size();
        }, () -> {
            source.addItem(items.toArray(new ItemStack[items.size()]));
        }, () -> {
            source.removeItem(items.toArray(new ItemStack[items.size()]));
        });
    }

    @Override
    public Transaction take(Entity entity) {
        if (!(entity instanceof InventoryHolder))
            return Transaction.fail();
        Inventory source = ((InventoryHolder) entity).getInventory();
        return Transaction.create(() -> {
            Map<Material, Integer> required = items.stream()
                    .collect(Collectors.toMap(k -> k.getType(), v -> v.getAmount()));
            boolean contains = true;
            for (Map.Entry<Material, Integer> entry : required.entrySet()) {
                if (!source.contains(entry.getKey(), entry.getValue())) {
                    contains = false;
                }
            }
            for (ItemStack item : items) {
                if (item.hasItemMeta() && !source.contains(item)) {
                    contains = false;
                }
            }
            return contains;
        }, () -> {
            source.removeItem(items.toArray(new ItemStack[items.size()]));
        }, () -> {
            source.addItem(items.toArray(new ItemStack[items.size()]));
        });
    }

    @Menu(title = "Item editor", dimensions = { 3, 9 })
    public static class ItemActionEditor extends InventoryMenuPage {
        private ItemAction base;
        private Consumer<NPCShopAction> callback;
        private MenuContext ctx;

        public ItemActionEditor() {
        }

        public ItemActionEditor(ItemAction base, Consumer<NPCShopAction> callback) {
            this.base = base;
            this.callback = callback;
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.ctx = ctx;
            for (int i = 0; i < 3 * 9; i++) {
                InventoryMenuSlot slot = ctx.getSlot(i);
                slot.clear();
                if (i < base.items.size()) {
                    slot.setItemStack(base.items.get(i).clone());
                }
                slot.setClickHandler(event -> {
                    event.setCancelled(true);
                    event.setCurrentItem(event.getCursorNonNull());
                });
            }
        }

        @Override
        public void onClose(HumanEntity player) {
            List<ItemStack> items = Lists.newArrayList();
            for (int i = 0; i < 3 * 9; i++) {
                if (ctx.getSlot(i).getCurrentItem() != null) {
                    items.add(ctx.getSlot(i).getCurrentItem().clone());
                }
            }
            callback.accept(items.isEmpty() ? null : new ItemAction(items));
        }
    }

    public static class ItemActionGUI implements GUI {
        @Override
        public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback) {
            return new ItemActionEditor(previous == null ? new ItemAction() : null, callback);
        }

        @Override
        public ItemStack createMenuItem(NPCShopAction previous) {
            String description = null;
            if (previous != null) {
                ItemAction old = (ItemAction) previous;
                description = old.items.size() + " items";
                for (int i = 0; i < old.items.size(); i++) {
                    ItemStack item = old.items.get(i);
                    description += "\n" + item.getAmount() + " " + Util.prettyEnum(item.getType());
                    if (i == 3) {
                        description += "...";
                        break;
                    }
                }
            }
            return Util.createItem(Material.CHEST, "Item", description);
        }

        @Override
        public boolean manages(NPCShopAction action) {
            return action instanceof ItemAction;
        }
    }
}