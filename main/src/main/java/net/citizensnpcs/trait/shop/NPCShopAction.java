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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.gui.InputMenus;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.PersisterRegistry;
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

    public abstract PendingAction grant(Entity entity);

    public abstract PendingAction take(Entity entity);

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
        public PendingAction grant(Entity entity) {
            return PendingAction.create(() -> {
                return true;
            }, () -> {
            }, () -> {
            });
        }

        @Override
        public PendingAction take(Entity entity) {
            return PendingAction.create(() -> {
                return true;
            }, () -> {
            }, () -> {
            });
        }

        public static class ItemActionGUI implements GUI {
            @Override
            public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback) {
                return null;
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
        public PendingAction grant(Entity entity) {
            if (!(entity instanceof OfflinePlayer))
                return PendingAction.fail();
            Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
            OfflinePlayer player = (OfflinePlayer) entity;
            return PendingAction.create(() -> {
                return true;
            }, () -> {
                economy.depositPlayer(player, money);
            }, () -> {
                economy.withdrawPlayer(player, money);
            });
        }

        @Override
        public PendingAction take(Entity entity) {
            if (!(entity instanceof OfflinePlayer))
                return PendingAction.fail();
            Economy economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
            OfflinePlayer player = (OfflinePlayer) entity;
            return PendingAction.create(() -> {
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

    public static class PendingAction {
        private final Runnable execute;
        private final Supplier<Boolean> possible;
        private final Runnable rollback;

        public PendingAction(Supplier<Boolean> isPossible, Runnable execute, Runnable rollback) {
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

        public static PendingAction create(Supplier<Boolean> isPossible, Runnable execute, Runnable rollback) {
            return new PendingAction(isPossible, execute, rollback);
        }

        public static PendingAction fail() {
            return new PendingAction(() -> false, () -> {
            }, () -> {
            });
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
        public PendingAction grant(Entity entity) {
            if (!(entity instanceof Player))
                return PendingAction.fail();
            Player player = (Player) entity;
            Permission perm = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
            return PendingAction.create(() -> {
                return true;
            }, () -> {
                for (String permission : permissions) {
                    perm.playerAdd(player, permission);
                }
            }, () -> {
                for (String permission : permissions) {
                    perm.playerRemove(player, permission);
                }
            });
        }

        @Override
        public PendingAction take(Entity entity) {
            if (!(entity instanceof Player))
                return PendingAction.fail();
            Player player = (Player) entity;
            Permission perm = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
            return PendingAction.create(() -> {
                for (String permission : permissions) {
                    if (!perm.playerHas(player, permission)) {
                        return false;
                    }
                }
                return true;
            }, () -> {
                for (String permission : permissions) {
                    perm.playerRemove(player, permission);
                }
            }, () -> {
                for (String permission : permissions) {
                    perm.playerAdd(player, permission);
                }
            });
        }

        public static class PermissionActionGUI implements GUI {
            private Boolean supported;

            @Override
            public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback) {
                return null;
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