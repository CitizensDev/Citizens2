package net.citizensnpcs.trait.shop;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.ChatColor;
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
import net.citizensnpcs.util.Util;

public class CommandAction extends NPCShopAction {
    @Persist
    public List<String> commands = Lists.newArrayList();
    @Persist
    public boolean op = false;
    @Persist
    public boolean server = false;

    public CommandAction() {
    }

    public CommandAction(List<String> commands) {
        this.commands = commands;
    }

    @Override
    public String describe() {
        String description = commands.size() + " 命令";
        for (int i = 0; i < commands.size(); i++) {
            description += "\n" + commands.get(i);
            if (i == 3) {
                description += "...";
                break;
            }
        }
        return description;
    }

    @Override
    public int getMaxRepeats(Entity entity) {
        return -1;
    }

    @Override
    public Transaction grant(Entity entity, int repeats) {
        if (!(entity instanceof Player))
            return Transaction.fail();
        Player player = (Player) entity;
        return Transaction.create(() -> true, () -> {
            for (int i = 0; i < repeats; i++) {
                for (String command : commands) {
                    Util.runCommand(null, player, command, op, !server);
                }
            }
        }, () -> {
        });
    }

    @Override
    public Transaction take(Entity entity, int repeats) {
        if (!(entity instanceof Player))
            return Transaction.fail();
        Player player = (Player) entity;
        return Transaction.create(() -> true, () -> {
            for (int i = 0; i < repeats; i++) {
                for (String command : commands) {
                    Util.runCommand(null, player, command, op, !server);
                }
            }
        }, () -> {
        });
    }

    @Menu(title = "命令编辑器", dimensions = { 4, 9 })
    public static class CommandActionEditor extends InventoryMenuPage {
        private CommandAction base;
        private Consumer<NPCShopAction> callback;

        public CommandActionEditor() {
        }

        public CommandActionEditor(CommandAction base, Consumer<NPCShopAction> callback) {
            this.base = base;
            this.callback = callback;
        }

        @Override
        public void initialise(MenuContext ctx) {
            for (int i = 0; i < 3 * 9; i++) {
                int idx = i;
                ctx.getSlot(i).clear();
                if (i < base.commands.size()) {
                    ctx.getSlot(i).setItemStack(new ItemStack(Material.FEATHER), "<f>设置命令",
                            "右键删除\n当前: " + base.commands.get(i));
                }
                ctx.getSlot(i).setClickHandler(event -> {
                    if (event.isRightClick()) {
                        event.setCancelled(true);
                        if (idx < base.commands.size()) {
                            base.commands.remove(idx);
                            ctx.getSlot(idx).setItemStack(null);
                        }
                        return;
                    }
                    ctx.getMenu().transition(InputMenus
                            .stringSetter(() -> idx < base.commands.size() ? base.commands.get(idx) : "", res -> {
                                if (res == null) {
                                    if (idx < base.commands.size()) {
                                        base.commands.remove(idx);
                                    }
                                    return;
                                }
                                if (idx < base.commands.size()) {
                                    base.commands.set(idx, res);
                                } else {
                                    base.commands.add(res);
                                }
                            }));
                });
            }
            ctx.getSlot(3 * 9 + 3).setItemStack(new ItemStack(Util.getFallbackMaterial("COMMAND_BLOCK", "COMMAND")),
                    "以服务器身份运行指令", base.server ? ChatColor.GREEN + "开" : ChatColor.RED + "关");
            ctx.getSlot(3 * 9 + 3).addClickHandler(InputMenus.toggler(res -> base.server = res, base.server));
            ctx.getSlot(3 * 9 + 4).setItemStack(
                    new ItemStack(Util.getFallbackMaterial("COMPARATOR", "REDSTONE_COMPARATOR")), "以OP 身份运行指令",
                    base.op ? ChatColor.GREEN + "开" : ChatColor.RED + "关");
            ctx.getSlot(3 * 9 + 4).addClickHandler(InputMenus.clickToggle(res -> {
                base.op = res;
                return res ? ChatColor.GREEN + "开" : ChatColor.RED + "关";
            }, base.server));
        }

        @Override
        public void onClose(HumanEntity player) {
            callback.accept(base.commands.isEmpty() ? null : base);
        }
    }

    public static class CommandActionGUI implements GUI {
        @Override
        public InventoryMenuPage createEditor(NPCShopAction previous, Consumer<NPCShopAction> callback) {
            return new CommandActionEditor(previous == null ? new CommandAction() : (CommandAction) previous, callback);
        }

        @Override
        public ItemStack createMenuItem(NPCShopAction previous) {
            String description = null;
            if (previous != null) {
                CommandAction old = (CommandAction) previous;
                description = old.describe();
            }
            return Util.createItem(Util.getFallbackMaterial("COMMAND_BLOCK", "COMMAND"), "Command", description);
        }

        @Override
        public boolean manages(NPCShopAction action) {
            return action instanceof CommandAction;
        }
    }
}
