package net.citizensnpcs.trait.shop;

import java.util.Arrays;
import java.util.List;
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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.gui.BooleanSlotHandler;
import net.citizensnpcs.api.gui.InputMenus;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.InventoryMenuSlot;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.jnbt.CompoundTag;
import net.citizensnpcs.api.jnbt.Tag;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

public class ItemAction extends NPCShopAction {
    @Persist
    public boolean compareSimilarity = true;
    @Persist
    public List<ItemStack> items = Lists.newArrayList();
    @Persist
    public List<String> metaFilter = Lists.newArrayList();
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
        List<Integer> req = items.stream().map(i -> i.getAmount()).collect(Collectors.toList());
        ItemStack[] contents = source.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() == Material.AIR)
                continue;
            if (requireUndamaged && stack.getItemMeta() instanceof Damageable
                    && ((Damageable) stack.getItemMeta()).getDamage() != 0)
                continue;
            for (int j = 0; j < items.size(); j++) {
                ItemStack match = items.get(j);
                if (req.get(j) <= 0)
                    continue;
                if (match.getType() != stack.getType())
                    continue;
                if (metaFilter.size() > 0 && !metaMatches(match, stack, metaFilter))
                    continue;
                if (compareSimilarity && !match.isSimilar(stack))
                    continue;

                int remaining = req.get(j);
                int taken = stack.getAmount() > remaining ? remaining : stack.getAmount();
                ItemStack res = filter.apply(stack, taken);
                if (res == null) {
                    source.clear(i);
                } else {
                    source.setItem(i, res);
                }
                req.set(j, remaining - taken);
            }
        }
        return req.stream().collect(Collectors.summingInt(n -> n)) <= 0;
    }

    @Override
    public String describe() {
        String description = items.size() + " items";
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            description += "\n" + item.getAmount() + " " + Util.prettyEnum(item.getType());
            if (i == 3) {
                description += "...";
                break;
            }
        }
        return description;
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

    private boolean metaMatches(ItemStack needle, ItemStack haystack, List<String> meta) {
        CompoundTag source = NMS.getNBT(needle);
        CompoundTag compare = NMS.getNBT(haystack);
        for (String nbt : meta) {
            String[] parts = nbt.split("\\.");
            Tag acc = source;
            Tag cmp = compare;
            for (int i = 0; i < parts.length; i++) {
                if (acc == null)
                    return false;
                if (cmp == null)
                    return false;
                if (i < parts.length - 1) {
                    if (!(acc instanceof CompoundTag) || !(cmp instanceof CompoundTag))
                        return false;
                    if (parts[i].equals(acc.getName()) && acc.getName().equals(cmp.getName()))
                        continue;
                    acc = ((CompoundTag) acc).getValue().get(parts[i]);
                    cmp = ((CompoundTag) cmp).getValue().get(parts[i]);
                    continue;
                }
                if (!acc.getName().equals(parts[i]) || !cmp.getName().equals(parts[i]))
                    return false;
                if (!acc.getValue().equals(cmp.getValue()))
                    return false;
            }
        }
        return true;
    }

    @Override
    public Transaction take(Entity entity) {
        if (!(entity instanceof InventoryHolder))
            return Transaction.fail();
        Inventory source = ((InventoryHolder) entity).getInventory();
        return Transaction.create(() -> {
            return containsItems(source, (stack, taken) -> stack);
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
                if (i < base.items.size()) {
                    slot.setItemStack(base.items.get(i).clone());
                }
                slot.setClickHandler(event -> {
                    event.setCancelled(true);
                    event.setCurrentItem(event.getCursorNonNull());
                });
            }

            ctx.getSlot(3 * 9 + 1).setItemStack(new ItemStack(Material.ANVIL), "Must have no damage",
                    base.requireUndamaged ? ChatColor.GREEN + "On" : ChatColor.RED + "Off");
            ctx.getSlot(3 * 9 + 1).addClickHandler(new BooleanSlotHandler((res) -> {
                base.requireUndamaged = res;
                return res ? ChatColor.GREEN + "On" : ChatColor.RED + "Off";
            }, base.requireUndamaged));
            ctx.getSlot(3 * 9 + 2).setItemStack(new ItemStack(Material.COMPARATOR), "Compare item similarity",
                    base.compareSimilarity ? ChatColor.GREEN + "On" : ChatColor.RED + "Off");
            ctx.getSlot(3 * 9 + 2).addClickHandler(new BooleanSlotHandler((res) -> {
                base.compareSimilarity = res;
                return res ? ChatColor.GREEN + "On" : ChatColor.RED + "Off";
            }, base.compareSimilarity));
            ctx.getSlot(3 * 9 + 3).setItemStack(new ItemStack(Material.BOOK), "NBT comparison filter",
                    Joiner.on("\n").join(base.metaFilter));
            ctx.getSlot(3 * 9 + 3)
                    .addClickHandler((event) -> ctx.getMenu()
                            .transition(InputMenus.stringSetter(() -> Joiner.on(',').join(base.metaFilter),
                                    res -> base.metaFilter = Arrays.asList(res.split(",")))));
        }

        @Override
        public void onClose(HumanEntity player) {
            List<ItemStack> items = Lists.newArrayList();
            for (int i = 0; i < 3 * 9; i++) {
                if (ctx.getSlot(i).getCurrentItem() != null) {
                    items.add(ctx.getSlot(i).getCurrentItem().clone());
                }
            }
            base.items = items;
            callback.accept(items.isEmpty() ? null : base);
        }
    }

    public static class ItemActionGUI implements GUI {
        @Override
        public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback) {
            return new ItemActionEditor(previous == null ? new ItemAction() : (ItemAction) previous, callback);
        }

        @Override
        public ItemStack createMenuItem(NPCShopAction previous) {
            String description = null;
            if (previous != null) {
                ItemAction old = (ItemAction) previous;
                description = old.describe();
            }
            return Util.createItem(Material.CHEST, "Item", description);
        }

        @Override
        public boolean manages(NPCShopAction action) {
            return action instanceof ItemAction;
        }
    }
}