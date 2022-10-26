package net.citizensnpcs.trait.shop;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.gui.BooleanSlotHandler;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.InventoryMenuSlot;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.util.Util;

public class ItemAction extends NPCShopAction {
    @Persist
    public List<ItemStack> items = Lists.newArrayList();
    @Persist
    public boolean requireUndamaged = true;

    public ItemAction() {
    }

    public ItemAction(ItemStack... items) {
        this(Arrays.asList(items));
    }

    public ItemAction(List<ItemStack> items) {
        this.items = items;
    }

    private boolean containsItems(Inventory source, BiFunction<ItemStack, Integer, ItemStack> filter) {
        Map<Material, Integer> required = items.stream()
                .collect(Collectors.toMap(k -> k.getType(), v -> v.getAmount()));
        ItemStack[] contents = source.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() == Material.AIR || !required.containsKey(stack.getType()))
                continue;
            if (requireUndamaged && stack.getItemMeta() instanceof Damageable
                    && ((Damageable) stack.getItemMeta()).getDamage() != 0)
                continue;
            int remaining = required.remove(stack.getType());
            int taken = stack.getAmount() > remaining ? remaining : stack.getAmount();
            ItemStack res = filter.apply(stack, taken);
            if (res == null) {
                source.clear(i);
            } else {
                source.setItem(i, res);
            }
            if (remaining - taken > 0) {
                required.put(stack.getType(), remaining - taken);
            }
        }
        return required.size() == 0;
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
            boolean contains = containsItems(source, (s, t) -> s);
            for (ItemStack item : items) {
                if (item.hasItemMeta() && !source.contains(item)) {
                    contains = false;
                }
            }
            return contains;
        }, () -> {
            containsItems(source, (stack, taken) -> {
                if (stack.getAmount() == taken) {
                    return null;
                } else {
                    stack.setAmount(stack.getAmount() - taken);
                    return stack;
                }
            });
        }, () -> {
            source.addItem(items.toArray(new ItemStack[items.size()]));
        });
    }

    @Menu(title = "Item editor", dimensions = { 4, 9 })
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
                if (base != null && i < base.items.size()) {
                    slot.setItemStack(base.items.get(i).clone());
                }
                slot.setClickHandler(event -> {
                    event.setCancelled(true);
                    event.setCurrentItem(event.getCursorNonNull());
                });
            }
            ctx.getSlot(3 * 9 + 1).setItemStack(new ItemStack(Material.ANVIL), "Must have no damage");
            ctx.getSlot(3 * 9 + 1).addClickHandler(new BooleanSlotHandler((res) -> {
                base.requireUndamaged = res;
                return res ? ChatColor.GREEN + "On" : ChatColor.RED + "Off";
            }, base == null ? false : base.requireUndamaged));
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