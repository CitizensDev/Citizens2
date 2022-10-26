package net.citizensnpcs.trait;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.gui.CitizensInventoryClickEvent;
import net.citizensnpcs.api.gui.ClickHandler;
import net.citizensnpcs.api.gui.InputMenus;
import net.citizensnpcs.api.gui.InputMenus.Choice;
import net.citizensnpcs.api.gui.InventoryMenu;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.InventoryMenuPattern;
import net.citizensnpcs.api.gui.InventoryMenuSlot;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.gui.MenuPattern;
import net.citizensnpcs.api.gui.MenuSlot;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.trait.shop.ItemAction;
import net.citizensnpcs.trait.shop.ItemAction.ItemActionGUI;
import net.citizensnpcs.trait.shop.MoneyAction;
import net.citizensnpcs.trait.shop.MoneyAction.MoneyActionGUI;
import net.citizensnpcs.trait.shop.NPCShopAction;
import net.citizensnpcs.trait.shop.NPCShopAction.GUI;
import net.citizensnpcs.trait.shop.NPCShopAction.Transaction;
import net.citizensnpcs.trait.shop.PermissionAction;
import net.citizensnpcs.trait.shop.PermissionAction.PermissionActionGUI;
import net.citizensnpcs.util.Util;

/**
 * Shop trait for NPC GUI shops.
 */
@TraitName("shop")
public class ShopTrait extends Trait {
    public ShopTrait() {
        super("shop");
    }

    public NPCShop getDefaultShop() {
        return StoredShops.NPC_SHOPS.computeIfAbsent(npc.getUniqueId().toString(), NPCShop::new);
    }

    public NPCShop getShop(String name) {
        return StoredShops.GLOBAL_SHOPS.computeIfAbsent(name, NPCShop::new);
    }

    public void onRightClick(Player player) {
        NPCShop shop = getDefaultShop();
        if (shop.openOnRightClick) {
            shop.display(player);
        }
    }

    public static class NPCShop {
        @Persist(value = "")
        private String name;
        @Persist
        private boolean openOnRightClick;
        @Persist(reify = true)
        private final List<NPCShopPage> pages = Lists.newArrayList();
        @Persist
        private String title;
        @Persist
        private ShopType type = ShopType.COMMAND;
        @Persist
        private String viewPermission;

        private NPCShop() {
        }

        private NPCShop(String name) {
            this.name = name;
        }

        public void display(Player sender) {
            if (viewPermission != null && !sender.hasPermission(viewPermission))
                return;
            if (pages.size() == 0) {
                sender.sendMessage(ChatColor.RED + "Empty shop");
                return;
            }
            InventoryMenu.createSelfRegistered(new NPCShopViewer(this, sender)).present(sender);
        }

        public void displayEditor(Player sender) {
            InventoryMenu.createSelfRegistered(new NPCShopSettings(this)).present(sender);
        }

        public String getName() {
            return name;
        }

        public NPCShopPage getOrCreatePage(int page) {
            while (pages.size() <= page) {
                pages.add(new NPCShopPage(page));
            }
            return pages.get(page);
        }

        public String getRequiredPermission() {
            return viewPermission;
        }

        public void removePage(int index) {
            for (int i = 0; i < pages.size(); i++) {
                if (pages.get(i).index == index) {
                    pages.remove(i--);
                    index = -1;
                } else if (index == -1) {
                    pages.get(i).index--;
                }
            }
        }

        public void setPermission(String permission) {
            this.viewPermission = permission;
            if (viewPermission != null && viewPermission.isEmpty()) {
                viewPermission = null;
            }
        }
    }

    @Menu(title = "NPC Shop Contents Editor", type = InventoryType.CHEST, dimensions = { 5, 9 })
    public static class NPCShopContentsEditor extends InventoryMenuPage {
        private MenuContext ctx;
        private int page = 0;
        private final NPCShop shop;

        public NPCShopContentsEditor(NPCShop shop) {
            this.shop = shop;
        }

        public void changePage(int newPage) {
            this.page = newPage;
            NPCShopPage shopPage = shop.getOrCreatePage(page);
            for (int i = 0; i < ctx.getInventory().getSize(); i++) {
                InventoryMenuSlot slot = ctx.getSlot(i);
                slot.clear();
                NPCShopItem item = shopPage.getItem(i);

                if (item != null) {
                    slot.setItemStack(item.display);
                }

                final int idx = i;
                slot.setClickHandler(evt -> {
                    evt.setCancelled(true);
                    ctx.clearSlots();
                    NPCShopItem display = item;
                    if (display == null) {
                        display = new NPCShopItem();
                        if (evt.getCursor() != null) {
                            display.display = evt.getCursor().clone();
                        }
                    }

                    ctx.getMenu().transition(new NPCShopItemEditor(display, modified -> {
                        if (modified == null) {
                            shopPage.removeItem(idx);
                        } else {
                            shopPage.setItem(idx, modified);
                        }
                    }));
                });
            }

            InventoryMenuSlot prev = ctx.getSlot(4 * 9 + 3);
            InventoryMenuSlot edit = ctx.getSlot(4 * 9 + 4);
            InventoryMenuSlot next = ctx.getSlot(4 * 9 + 5);
            prev.clear();
            if (page > 0) {
                prev.setItemStack(new ItemStack(Material.FEATHER, 1), "Previous page (" + (page) + ")");
                prev.setClickHandler(evt -> {
                    evt.setCancelled(true);
                    changePage(page - 1);
                });
            }

            next.setItemStack(new ItemStack(Material.FEATHER, 1),
                    page + 1 >= shop.pages.size() ? "New page" : "Next page (" + (page + 1) + ")");
            next.setClickHandler(evt -> {
                evt.setCancelled(true);
                changePage(page + 1);
            });

            edit.setItemStack(new ItemStack(Material.BOOK), "Edit page");
            edit.setClickHandler(evt -> {
                evt.setCancelled(true);
                ctx.getMenu().transition(new NPCShopPageSettings(shop.getOrCreatePage(page)));
            });
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.ctx = ctx;
            if (ctx.data().containsKey("removePage")) {
                int index = (int) ctx.data().remove("removePage");
                shop.removePage(index);
                page = Math.max(page - 1, 0);
            }
            changePage(page);
        }
    }

    public static class NPCShopItem implements Cloneable {
        @Persist
        private final List<NPCShopAction> cost = Lists.newArrayList();
        @Persist
        private ItemStack display;
        @Persist
        private final List<NPCShopAction> result = Lists.newArrayList();

        private void changeAction(List<NPCShopAction> source, Function<NPCShopAction, Boolean> filter,
                NPCShopAction delta) {
            for (int i = 0; i < source.size(); i++) {
                if (filter.apply(source.get(i))) {
                    if (delta == null) {
                        source.remove(i);
                    } else {
                        source.set(i, delta);
                    }
                    return;
                }
            }
            if (delta != null) {
                source.add(delta);
            }
        }

        public void changeCost(Function<NPCShopAction, Boolean> filter, NPCShopAction cost) {
            changeAction(this.cost, filter, cost);
        }

        public void changeResult(Function<NPCShopAction, Boolean> filter, NPCShopAction result) {
            changeAction(this.result, filter, result);
        }

        @Override
        public NPCShopItem clone() {
            try {
                return (NPCShopItem) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new Error(e);
            }
        }

        public List<Transaction> execute(List<NPCShopAction> actions, Function<NPCShopAction, Transaction> func) {
            List<Transaction> pending = Lists.newArrayList();
            for (NPCShopAction action : actions) {
                Transaction take = func.apply(action);
                if (!take.isPossible()) {
                    pending.forEach(a -> a.rollback());
                    return null;
                } else {
                    take.run();
                    pending.add(take);
                }
            }
            return pending;
        }

        public ItemStack getDisplayItem(Player player) {
            if (display == null)
                return null;
            ItemStack stack = display.clone();
            ItemMeta meta = stack.getItemMeta();
            if (meta.hasDisplayName()) {
                meta.setDisplayName(Placeholders.replace(meta.getDisplayName(), player));
            }
            if (meta.hasLore()) {
                meta.setLore(Lists.transform(meta.getLore(), line -> Placeholders.replace(line, player)));
            }
            stack.setItemMeta(meta);
            return stack;
        }

        public void onClick(NPCShop shop, CitizensInventoryClickEvent event) {
            if (shop.type == ShopType.COMMAND)
                return;
            List<Transaction> take = execute(cost, action -> action.take(event.getWhoClicked()));
            if (take == null)
                return;
            if (execute(result, action -> action.grant(event.getWhoClicked())) == null) {
                take.forEach(a -> a.rollback());
            }
        }
    }

    @Menu(title = "NPC Shop Item Editor", type = InventoryType.CHEST, dimensions = { 6, 9 })
    @MenuSlot(slot = { 3, 4 }, material = Material.DISPENSER, amount = 1, title = "<f>Place display item below")
    public static class NPCShopItemEditor extends InventoryMenuPage {
        @MenuPattern(
                offset = { 0, 6 },
                slots = { @MenuSlot(pat = 'x', material = Material.AIR) },
                value = "x x\n x \nx x")
        private InventoryMenuPattern actionItems;
        private final Consumer<NPCShopItem> callback;
        @MenuPattern(
                offset = { 0, 0 },
                slots = { @MenuSlot(pat = 'x', material = Material.AIR) },
                value = "x x\n x \nx x")
        private InventoryMenuPattern costItems;
        private MenuContext ctx;
        private final NPCShopItem modified;
        private NPCShopItem original;

        public NPCShopItemEditor(NPCShopItem item, Consumer<NPCShopItem> consumer) {
            this.original = item;
            this.modified = original.clone();
            this.callback = consumer;
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.ctx = ctx;
            if (modified.display != null) {
                ctx.getSlot(9 * 4 + 4).setItemStack(modified.display);
            }
            int pos = 0;
            for (GUI template : NPCShopAction.getGUIs()) {
                if (template.createMenuItem(null) == null)
                    continue;

                NPCShopAction oldCost = modified.cost.stream().filter(template::manages).findFirst().orElse(null);
                costItems.getSlots().get(pos)
                        .setItemStack(Util.editTitle(template.createMenuItem(oldCost), title -> title + " Cost"));
                costItems.getSlots().get(pos).setClickHandler(event -> {
                    event.setCancelled(true);
                    ctx.getMenu().transition(
                            template.createEditor(oldCost, cost -> modified.changeCost(template::manages, cost)));
                });

                NPCShopAction oldResult = modified.result.stream().filter(template::manages).findFirst().orElse(null);
                actionItems.getSlots().get(pos)
                        .setItemStack(Util.editTitle(template.createMenuItem(oldResult), title -> title + " Result"));
                actionItems.getSlots().get(pos).setClickHandler(event -> {
                    event.setCancelled(true);
                    ctx.getMenu().transition(template.createEditor(oldResult,
                            result -> modified.changeResult(template::manages, result)));
                });

                pos++;
            }
        }

        @MenuSlot(slot = { 5, 3 }, material = Material.REDSTONE_BLOCK, amount = 1, title = "<7>Cancel")
        public void onCancel(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.getMenu().transitionBack();
        }

        @Override
        public void onClose(HumanEntity who) {
            if (original != null && original.display == null) {
                original = null;
            }
            callback.accept(original);
        }

        @MenuSlot(slot = { 4, 5 }, material = Material.BOOK, amount = 1, title = "<f>Set description")
        public void onEditDescription(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            if (modified.display == null)
                return;
            ctx.getMenu()
                    .transition(InputMenus.stringSetter(() -> modified.display.getItemMeta().hasLore()
                            ? Joiner.on("<br>").skipNulls().join(modified.display.getItemMeta().getLore())
                            : "", description -> {
                                ItemMeta meta = modified.display.getItemMeta();
                                meta.setLore(Lists
                                        .newArrayList(Splitter.on("<br>").split(Colorizer.parseColors(description))));
                                modified.display.setItemMeta(meta);
                            }));
        }

        @MenuSlot(slot = { 4, 3 }, material = Material.FEATHER, amount = 1, title = "<f>Set name")
        public void onEditName(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            if (modified.display == null)
                return;
            ctx.getMenu().transition(InputMenus.stringSetter(modified.display.getItemMeta()::getDisplayName, name -> {
                ItemMeta meta = modified.display.getItemMeta();
                meta.setDisplayName(ChatColor.RESET + Colorizer.parseColors(name));
                modified.display.setItemMeta(meta);
            }));
        }

        @ClickHandler(slot = { 4, 4 })
        public void onModifyDisplayItem(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            if (event.getCursor() != null) {
                event.setCurrentItem(event.getCursor());
                modified.display = event.getCursor().clone();
            } else {
                event.setCurrentItem(null);
                modified.display = null;
            }
        }

        @MenuSlot(slot = { 5, 4 }, material = Material.TNT, amount = 1, title = "<c>Remove")
        public void onRemove(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            original = null;
            event.setCancelled(true);
            ctx.getMenu().transitionBack();
        }

        @MenuSlot(slot = { 5, 5 }, material = Material.EMERALD_BLOCK, amount = 1, title = "<a>Save")
        public void onSave(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            original = modified;
            event.setCancelled(true);
            ctx.getMenu().transitionBack();
        }
    }

    public static class NPCShopPage {
        @Persist("$key")
        private int index;
        @Persist(keyType = Integer.class, reify = true)
        private final Map<Integer, NPCShopItem> items = Maps.newHashMap();
        @Persist
        private String title;

        private NPCShopPage() {
        }

        public NPCShopPage(int page) {
            this.index = page;
        }

        public NPCShopItem getItem(int idx) {
            return items.get(idx);
        }

        public void removeItem(int idx) {
            items.remove(idx);
        }

        public void setItem(int idx, NPCShopItem modified) {
            items.put(idx, modified);
        }
    }

    @Menu(title = "NPC Shop Page Editor", type = InventoryType.CHEST, dimensions = { 5, 9 })
    public static class NPCShopPageSettings extends InventoryMenuPage {
        private MenuContext ctx;
        private final NPCShopPage page;

        public NPCShopPageSettings(NPCShopPage page) {
            this.page = page;
        }

        @MenuSlot(slot = { 0, 4 }, material = Material.FEATHER, amount = 1)
        public void editPageTitle(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.getMenu().transition(InputMenus.stringSetter(() -> page.title, newTitle -> {
                page.title = newTitle.isEmpty() ? null : newTitle;
            }));
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.ctx = ctx;
            ctx.getSlot(4).setDescription("Set page title<br>Currently: " + page.title);
        }

        @MenuSlot(slot = { 4, 4 }, material = Material.TNT, amount = 1, title = "<c>Remove page")
        public void removePage(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.data().put("removePage", page.index);
            ctx.getMenu().transitionBack();
        }
    }

    @Menu(title = "NPC Shop Editor", type = InventoryType.CHEST, dimensions = { 1, 9 })
    public static class NPCShopSettings extends InventoryMenuPage {
        private MenuContext ctx;
        private final NPCShop shop;

        public NPCShopSettings(NPCShop shop) {
            this.shop = shop;
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.ctx = ctx;
            ctx.getSlot(2).setDescription("<f>Edit shop view permission<br>" + shop.getRequiredPermission());
            ctx.getSlot(6).setDescription("<f>Edit shop title<br>" + shop.title);
            ctx.getSlot(8).setDescription("<f>Show shop on right click<br>" + shop.openOnRightClick);
        }

        @MenuSlot(slot = { 0, 4 }, material = Material.FEATHER, amount = 1, title = "<f>Edit shop items")
        public void onEditItems(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.getMenu().transition(new NPCShopContentsEditor(shop));
        }

        @MenuSlot(slot = { 0, 2 }, material = Material.OAK_SIGN, amount = 1)
        public void onPermissionChange(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.getMenu().transition(InputMenus.stringSetter(shop::getRequiredPermission, shop::setPermission));
        }

        @MenuSlot(slot = { 0, 6 }, material = Material.NAME_TAG, amount = 1)
        public void onSetTitle(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.getMenu().transition(InputMenus.stringSetter(() -> shop.title, newTitle -> {
                shop.title = newTitle.isEmpty() ? null : newTitle;
            }));
        }

        @MenuSlot(slot = { 0, 0 }, material = Material.BOOK, amount = 1, title = "<f>Edit shop type")
        public void onShopTypeChange(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.getMenu().transition(InputMenus.<ShopType> picker("Edit shop type", chosen -> {
                shop.type = chosen.getValue();
            }, Choice.<ShopType> of(ShopType.BUY, Material.DIAMOND, "Players buy items", shop.type == ShopType.BUY),
                    Choice.of(ShopType.SELL, Material.EMERALD, "Players sell items", shop.type == ShopType.SELL),
                    Choice.of(ShopType.COMMAND, Material.ENDER_EYE, "Clicks trigger commands only",
                            shop.type == ShopType.COMMAND)));
        }

        @MenuSlot(slot = { 0, 8 }, material = Material.COMMAND_BLOCK, amount = 1)
        public void onToggleRightClick(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            shop.openOnRightClick = !shop.openOnRightClick;
            ctx.getSlot(8).setDescription("<f>Show shop on right click<br>" + shop.openOnRightClick);
        }
    }

    @Menu(title = "Shop", type = InventoryType.CHEST, dimensions = { 5, 9 })
    public static class NPCShopViewer extends InventoryMenuPage {
        private MenuContext ctx;
        private int currentPage = 0;
        private final Player player;
        private final NPCShop shop;

        public NPCShopViewer(NPCShop shop, Player player) {
            this.shop = shop;
            this.player = player;
        }

        public void changePage(int newPage) {
            this.currentPage = newPage;
            NPCShopPage page = shop.pages.get(currentPage);
            if (page.title != null && !page.title.isEmpty()) {
                Bukkit.getScheduler().runTaskLater(CitizensAPI.getPlugin(), () -> {
                    ctx.setTitle(Placeholders.replace(page.title, player));
                }, 1);
            }
            for (int i = 0; i < ctx.getInventory().getSize(); i++) {
                ctx.getSlot(i).clear();
                NPCShopItem item = page.getItem(i);
                if (item == null)
                    continue;

                ctx.getSlot(i).setItemStack(item.getDisplayItem(player));
                ctx.getSlot(i).setClickHandler(evt -> {
                    evt.setCancelled(true);
                    item.onClick(shop, evt);
                });
            }
            InventoryMenuSlot prev = ctx.getSlot(4 * 9 + 3);
            InventoryMenuSlot next = ctx.getSlot(4 * 9 + 5);
            prev.clear();
            if (currentPage > 0) {
                prev.setItemStack(new ItemStack(Material.FEATHER, 1), "Previous page (" + (currentPage) + ")");
                prev.setClickHandler(evt -> {
                    evt.setCancelled(true);
                    changePage(currentPage - 1);
                });
            }

            next.clear();
            if (currentPage + 1 < shop.pages.size()) {
                next.setItemStack(new ItemStack(Material.FEATHER, 1), "Next page (" + (currentPage + 1) + ")");
                next.setClickHandler(evt -> {
                    evt.setCancelled(true);
                    changePage(currentPage + 1);
                });
            }
        }

        @Override
        public Inventory createInventory(String title) {
            return Bukkit.createInventory(null, 45,
                    shop.title == null || shop.title.isEmpty() ? "Shop" : Placeholders.replace(shop.title, player));
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.ctx = ctx;
            changePage(currentPage);
        }
    }

    public enum ShopType {
        BUY,
        COMMAND,
        SELL;
    }

    private static class StoredShops {
        @Persist(value = "global", reify = true)
        private static Map<String, NPCShop> GLOBAL_SHOPS = Maps.newHashMap();
        @Persist(value = "npc", reify = true)
        private static Map<String, NPCShop> NPC_SHOPS = Maps.newHashMap();
    }

    public static void loadShops(DataKey root) {
        SAVED = PersistenceLoader.load(StoredShops.class, root);
    }

    public static void saveShops(DataKey root) {
        PersistenceLoader.save(SAVED, root);
    }

    private static StoredShops SAVED = new StoredShops();
    static {
        NPCShopAction.register(ItemAction.class, "items", new ItemActionGUI());
        NPCShopAction.register(PermissionAction.class, "permissions", new PermissionActionGUI());
        NPCShopAction.register(MoneyAction.class, "money", new MoneyActionGUI());
    }
}