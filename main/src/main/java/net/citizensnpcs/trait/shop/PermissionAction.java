package net.citizensnpcs.trait.shop;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.gui.InputMenus;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.util.Util;
import net.milkbowl.vault.permission.Permission;

public class PermissionAction extends NPCShopAction {
    @Persist
    public List<String> permissions = Lists.newArrayList();

    public PermissionAction() {
    }

    public PermissionAction(List<String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String describe() {
        String description = permissions.size() + " permissions";
        for (int i = 0; i < permissions.size(); i++) {
            description += "\n" + permissions.get(i);
            if (i == 3) {
                description += "...";
                break;
            }
        }
        return description;
    }

    @Override
    public int getMaxRepeats(Entity entity, ItemStack[] inventory) {
        return -1;
    }

    @Override
    public Transaction grant(Entity entity, ItemStack[] inventory, int repeats) {
        if (!(entity instanceof Player))
            return Transaction.fail();
        Player player = (Player) entity;
        Permission perm = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
        return Transaction.create(() -> true, () -> {
            for (String permission : permissions) {
                perm.playerAdd(null, player, Placeholders.replace(permission, player));
            }
        }, () -> {
            for (String permission : permissions) {
                perm.playerRemove(null, player, Placeholders.replace(permission, player));
            }
        });
    }

    @Override
    public Transaction take(Entity entity, ItemStack[] inventory, int repeats) {
        if (!(entity instanceof Player))
            return Transaction.fail();
        Player player = (Player) entity;
        Permission perm = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider();
        return Transaction.create(() -> {
            for (String permission : permissions) {
                if (!perm.playerHas(player, Placeholders.replace(permission, player)))
                    return false;
            }
            return true;
        }, () -> {
            for (String permission : permissions) {
                perm.playerRemove(null, player, Placeholders.replace(permission, player));
            }
        }, () -> {
            for (String permission : permissions) {
                perm.playerAdd(null, player, Placeholders.replace(permission, player));
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
                int idx = i;
                ctx.getSlot(i).clear();
                if (i < base.permissions.size()) {
                    ctx.getSlot(i).setItemStack(new ItemStack(Material.FEATHER), "<f>Set permission",
                            "Right click to remove\nCurrently: " + base.permissions.get(i));
                }
                ctx.getSlot(i).setClickHandler(event -> {
                    if (event.isRightClick()) {
                        event.setCancelled(true);
                        if (idx < base.permissions.size()) {
                            base.permissions.remove(idx);
                            ctx.getSlot(idx).setItemStack(null);
                        }
                        return;
                    }
                    ctx.getMenu().transition(InputMenus
                            .stringSetter(() -> idx < base.permissions.size() ? base.permissions.get(idx) : "", res -> {
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
            return new PermissionActionEditor(previous == null ? new PermissionAction() : (PermissionAction) previous,
                    callback);
        }

        @Override
        public ItemStack createMenuItem(NPCShopAction previous) {
            if (supported == null) {
                try {
                    supported = Bukkit.getServicesManager().getRegistration(Permission.class).getProvider() != null;
                } catch (Throwable t) {
                    supported = false;
                }
            }
            if (!supported)
                return null;
            String description = null;
            if (previous != null) {
                PermissionAction old = (PermissionAction) previous;
                description = old.describe();
            }
            return Util.createItem(Material.PAPER, "Permission", description);
        }

        @Override
        public boolean manages(NPCShopAction action) {
            return action instanceof PermissionAction;
        }
    }
}