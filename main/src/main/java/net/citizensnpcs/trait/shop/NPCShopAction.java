package net.citizensnpcs.trait.shop;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.gui.InputMenus;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.InventoryMenuSlot;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.PersisterRegistry;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.trait.shop.NPCShopAction.ItemAction.ItemActionGUI;
import net.citizensnpcs.trait.shop.NPCShopAction.MoneyAction.MoneyActionGUI;
import net.citizensnpcs.trait.shop.NPCShopAction.PermissionAction.PermissionActionGUI;
import net.citizensnpcs.util.Util;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public abstract class NPCShopAction implements Cloneable {
    @Override
    public NPCShopAction clone() {
        try {
            return (NPCShopAction) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    public abstract Transaction grant(Entity entity);

    public abstract Transaction take(Entity entity);

    public static interface GUI {
        public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback);

        public ItemStack createMenuItem();

        public boolean manages(NPCShopAction action);
    }

    public static class ItemAction extends NPCShopAction {
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
                return source.all(Material.AIR).size() > items.size();
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
                return source.all(Material.AIR).size() > items.size();
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
                    slot.addClickHandler(event -> {
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
            public ItemStack createMenuItem() {
                return Util.createItem(Material.CHEST, "Item");
            }

            @Override
            public boolean manages(NPCShopAction action) {
                return action instanceof ItemAction;
            }
        }
    }

    public static class MoneyAction extends NPCShopAction {
        @Persist
        public double money;

        public MoneyAction() {
        }

        @Override
        public Transaction grant(Entity entity) {
            if (!(entity instanceof OfflinePlayer))
                return Transaction.fail();
            Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
            OfflinePlayer player = (OfflinePlayer) entity;
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
            if (!(entity instanceof OfflinePlayer))
                return Transaction.fail();
            Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
            OfflinePlayer player = (OfflinePlayer) entity;
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
            public ItemStack createMenuItem() {
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
                return Util.createItem(Material.GOLD_INGOT, "Money");
            }

            @Override
            public boolean manages(NPCShopAction action) {
                return action instanceof MoneyAction;
            }
        }
    }

    public static class PermissionAction extends NPCShopAction {
        @Persist
        public List<String> permissions = Lists.newArrayList();

        public PermissionAction() {
        }

        public PermissionAction(List<String> permissions) {
            this.permissions = permissions;
        }

        @Override
        public Transaction grant(Entity entity) {
            if (!(entity instanceof Player))
                return Transaction.fail();
            Player player = (Player) entity;
            Permission perm = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
            return Transaction.create(() -> {
                return true;
            }, () -> {
                for (String permission : permissions) {
                    perm.playerAdd(player, Placeholders.replace(permission, player));
                }
            }, () -> {
                for (String permission : permissions) {
                    perm.playerRemove(player, Placeholders.replace(permission, player));
                }
            });
        }

        @Override
        public Transaction take(Entity entity) {
            if (!(entity instanceof Player))
                return Transaction.fail();
            Player player = (Player) entity;
            Permission perm = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
            return Transaction.create(() -> {
                for (String permission : permissions) {
                    if (!perm.playerHas(player, Placeholders.replace(permission, player))) {
                        return false;
                    }
                }
                return true;
            }, () -> {
                for (String permission : permissions) {
                    perm.playerRemove(player, Placeholders.replace(permission, player));
                }
            }, () -> {
                for (String permission : permissions) {
                    perm.playerAdd(player, Placeholders.replace(permission, player));
                }
            });
        }

        @Menu(title = "Permissions editor", dimensions = { 3, 9 })
        public static class PermissionActionEditor extends InventoryMenuPage {
            private PermissionAction base;
            private Consumer<NPCShopAction> callback;

            public PermissionActionEditor() {
            }

            public PermissionActionEditor(PermissionAction base, Consumer<NPCShopAction> callback) {
                this.base = base;
                this.callback = callback;
            }

            @Override
            public void initialise(MenuContext ctx) {
                for (int i = 0; i < 3 * 9; i++) {
                    final int idx = i;
                    ctx.getSlot(i).clear();
                    if (i < base.permissions.size()) {
                        ctx.getSlot(i).setItemStack(new ItemStack(Material.FEATHER), "<f>Set permission",
                                "Right click to remove\nCurrently: " + base.permissions.get(i));
                    }
                    ctx.getSlot(i).addClickHandler(event -> {
                        if (event.isRightClick()) {
                            if (idx < base.permissions.size()) {
                                base.permissions.remove(idx);
                                ctx.getSlot(idx).setItemStack(null);
                            }
                            return;
                        }
                        ctx.getMenu().transition(InputMenus.stringSetter(
                                () -> idx < base.permissions.size() ? base.permissions.get(idx) : "", (res) -> {
                                    if (res == null) {
                                        if (idx < base.permissions.size()) {
                                            base.permissions.remove(idx);
                                        }
                                        return;
                                    }
                                    if (idx < base.permissions.size()) {
                                        base.permissions.set(idx, res);
                                    } else {
                                        base.permissions.add(res);
                                    }
                                }));
                    });
                }
            }

            @Override
            public void onClose(HumanEntity player) {
                callback.accept(base.permissions.isEmpty() ? null : base);
            }
        }

        public static class PermissionActionGUI implements GUI {
            private Boolean supported;

            @Override
            public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback) {
                return new PermissionActionEditor(
                        previous == null ? new PermissionAction() : (PermissionAction) previous, callback);
            }

            @Override
            public ItemStack createMenuItem() {
                if (supported == null) {
                    try {
                        supported = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider() != null;
                    } catch (Throwable t) {
                        supported = false;
                    }
                }
                if (!supported) {
                    return null;
                }
                return Util.createItem(Util.getFallbackMaterial("OAK_SIGN", "SIGN"), "Permission");
            }

            @Override
            public boolean manages(NPCShopAction action) {
                return action instanceof PermissionAction;
            }
        }
    }

    public static class Transaction {
        private final Runnable execute;
        private final Supplier<Boolean> possible;
        private final Runnable rollback;

        public Transaction(Supplier<Boolean> isPossible, Runnable execute, Runnable rollback) {
            this.possible = isPossible;
            this.execute = execute;
            this.rollback = rollback;
        }

        public boolean isPossible() {
            return possible.get();
        }

        public void rollback() {
            rollback.run();
        }

        public void run() {
            execute.run();
        }

        public static Transaction create(Supplier<Boolean> isPossible, Runnable execute, Runnable rollback) {
            return new Transaction(isPossible, execute, rollback);
        }

        public static Transaction fail() {
            return new Transaction(() -> false, () -> {
            }, () -> {
            });
        }
    }

    public static Iterable<GUI> getGUIs() {
        return GUI.values();
    }

    public static void register(Class<? extends NPCShopAction> clazz, String type, GUI gui) {
        REGISTRY.register(type, clazz);
        GUI.put(clazz, gui);
    }

    private static final Map<Class<? extends NPCShopAction>, GUI> GUI = new WeakHashMap<>();
    private static final PersisterRegistry<NPCShopAction> REGISTRY = PersistenceLoader
            .createRegistry(NPCShopAction.class);
    static {
        register(ItemAction.class, "items", new ItemActionGUI());
        register(PermissionAction.class, "permissions", new PermissionActionGUI());
        register(MoneyAction.class, "money", new MoneyActionGUI());
    }
}