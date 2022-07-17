package net.citizensnpcs.trait;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.citizensnpcs.api.gui.CitizensInventoryClickEvent;
import net.citizensnpcs.api.gui.ClickHandler;
import net.citizensnpcs.api.gui.InputMenus;
import net.citizensnpcs.api.gui.InputMenus.Choice;
import net.citizensnpcs.api.gui.InventoryMenu;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.InventoryMenuSlot;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.gui.MenuSlot;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

/**
 * Shop trait for NPC GUI shops.
 */
@TraitName("shop")
public class ShopTrait extends Trait {
    public ShopTrait() {
        super("shop");
    }

    public NPCShop getDefaultShop() {
        return NPC_SHOPS.computeIfAbsent(npc.getUniqueId().toString(), NPCShop::new);
    }

    public NPCShop getShop(String name) {
        return SHOPS.computeIfAbsent(name, NPCShop::new);
    }

    public static class NPCShop {
        @Persist
        private final String name;
        @Persist(reify = true)
        private final List<NPCShopPage> pages = Lists.newArrayList();
        @Persist
        private String title;
        @Persist
        private ShopType type = ShopType.COMMAND;
        @Persist
        private String viewPermission;

        private NPCShop(String name) {
            this.name = name;
        }

        public void display(Player sender) {
            if (viewPermission != null && !sender.hasPermission(viewPermission))
                return;
        }

        public void displayEditor(Player sender) {
            InventoryMenu.createSelfRegistered(new NPCShopEditor(this)).present(sender);
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
            NPCShopPage sp = shop.getOrCreatePage(page);
            for (int i = 0; i < ctx.getInventory().getSize(); i++) {
                ctx.getSlot(i).clear();
                NPCShopItem item = sp.items.get(i);
                final int idx = i;
                ctx.getSlot(i).addClickHandler(evt -> {
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
                            sp.items.remove(idx);
                        } else {
                            sp.items.put(idx, modified);
                        }
                    }));
                });

                if (item == null)
                    continue;

                ctx.getSlot(i).setItemStack(item.display);
            }
            InventoryMenuSlot prev = ctx.getSlot(4 * 9 + 3);
            InventoryMenuSlot edit = ctx.getSlot(4 * 9 + 4);
            InventoryMenuSlot next = ctx.getSlot(4 * 9 + 5);
            prev.clear();
            if (page > 0) {
                prev.setItemStack(new ItemStack(Material.FEATHER, 1), "Previous page (" + (page) + ")");
                prev.addClickHandler(evt -> {
                    evt.setCancelled(true);
                    changePage(page - 1);
                });
            }

            next.clear();
            next.setItemStack(new ItemStack(Material.FEATHER, 1),
                    page + 1 >= shop.pages.size() ? "New page" : "Next page (" + (page + 1) + ")");
            next.addClickHandler(evt -> {
                evt.setCancelled(true);
                changePage(page + 1);
            });

            edit.clear();
            edit.setItemStack(new ItemStack(Material.BOOK), "Edit page");
            edit.addClickHandler(evt -> {
                evt.setCancelled(true);
                ctx.getMenu().transition(new NPCShopPageEditor(shop.getOrCreatePage(page)));
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

    @Menu(title = "NPC Shop Editor", type = InventoryType.HOPPER, dimensions = { 0, 5 })
    @MenuSlot(slot = { 0, 0 }, material = Material.BOOK, amount = 1, lore = "Edit shop type")
    @MenuSlot(slot = { 0, 2 }, material = Material.OAK_SIGN, amount = 1, lore = "Edit shop permission")
    @MenuSlot(slot = { 0, 4 }, material = Material.FEATHER, amount = 1, lore = "Edit shop items")
    public static class NPCShopEditor extends InventoryMenuPage {
        private MenuContext ctx;
        private final NPCShop shop;

        public NPCShopEditor(NPCShop shop) {
            this.shop = shop;
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.ctx = ctx;
        }

        @ClickHandler(slot = { 0, 4 })
        public void onEditItems(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.getMenu().transition(new NPCShopContentsEditor(shop));
        }

        @ClickHandler(slot = { 0, 2 })
        public void onPermissionChange(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.getMenu().transition(InputMenus.stringSetter(shop::getRequiredPermission, shop::setPermission));
        }

        @ClickHandler(slot = { 0, 0 })
        public void onShopTypeChange(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.getMenu().transition(InputMenus.<ShopType> picker("Edit shop type", chosen -> {
                shop.type = chosen.getValue();
            }, Choice.<ShopType> of(ShopType.BUY, Material.DIAMOND, "Players buy items", shop.type == ShopType.BUY),
                    Choice.of(ShopType.SELL, Material.EMERALD, "Players sell items", shop.type == ShopType.SELL),
                    Choice.of(ShopType.COMMAND, Material.ENDER_EYE, "Clicks trigger commands only",
                            shop.type == ShopType.COMMAND)));
        }
    }

    public static class NPCShopItem implements Cloneable {
        @Persist
        private int cost;
        @Persist
        private ItemStack display;

        @Override
        public NPCShopItem clone() {
            try {
                return (NPCShopItem) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new Error(e);
            }
        }
    }

    @Menu(title = "NPC Shop Item Editor", type = InventoryType.CHEST, dimensions = { 5, 9 })
    @MenuSlot(slot = { 0, 4 }, material = Material.DISPENSER, amount = 1, title = "Display item")
    public static class NPCShopItemEditor extends InventoryMenuPage {
        private final Consumer<NPCShopItem> consumer;
        private MenuContext ctx;
        private final NPCShopItem modified;
        private NPCShopItem original;

        public NPCShopItemEditor(NPCShopItem item, Consumer<NPCShopItem> ret) {
            this.original = item;
            this.modified = original.clone();
            this.consumer = ret;
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.ctx = ctx;
            if (modified.display != null) {
                ctx.getSlot(9 + 4).setItemStack(modified.display);
            }
        }

        @ClickHandler(slot = { 4, 3 })
        @MenuSlot(slot = { 4, 3 }, material = Material.REDSTONE_BLOCK, amount = 1, title = "Cancel")
        public void onCancel(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.getMenu().transitionBack();
        }

        @Override
        public void onClose(HumanEntity who) {
            if (original.display == null) {
                original = null;
            }
            consumer.accept(original);
        }

        @MenuSlot(slot = { 0, 3 }, material = Material.BOOK, amount = 1, title = "Set description")
        @ClickHandler(slot = { 0, 3 })
        public void onEditDescription(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            if (modified.display != null) {
                ctx.getMenu()
                        .transition(InputMenus.stringSetter(
                                () -> Joiner.on("<br>").skipNulls().join(modified.display.getItemMeta().getLore()),
                                description -> {
                                    ItemMeta meta = modified.display.getItemMeta();
                                    meta.setLore(Lists.newArrayList(Splitter.on("<br>").split(description)));
                                    modified.display.setItemMeta(meta);
                                }));
            }
        }

        @MenuSlot(slot = { 0, 5 }, material = Material.FEATHER, amount = 1, title = "Set name")
        @ClickHandler(slot = { 0, 5 })
        public void onEditName(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            if (modified.display != null) {
                ctx.getMenu()
                        .transition(InputMenus.stringSetter(modified.display.getItemMeta()::getDisplayName, name -> {
                            ItemMeta meta = modified.display.getItemMeta();
                            meta.setDisplayName(name);
                            modified.display.setItemMeta(meta);
                        }));
            }
        }

        @ClickHandler(slot = { 1, 4 })
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

        @ClickHandler(slot = { 4, 4 })
        @MenuSlot(slot = { 4, 4 }, material = Material.TNT, amount = 1, lore = "<c>Remove")
        public void onRemove(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            original = null;
            event.setCancelled(true);
            ctx.getMenu().transitionBack();
        }

        @ClickHandler(slot = { 4, 5 })
        @MenuSlot(slot = { 4, 5 }, material = Material.EMERALD_BLOCK, amount = 1, lore = "Save")
        public void onSave(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            original = modified;
            event.setCancelled(true);
            ctx.getMenu().transitionBack();
        }
    }

    public static class NPCShopPage {
        @Persist("")
        private int index;
        @Persist(reify = true)
        private final Map<Integer, NPCShopItem> items = Maps.newHashMap();
        @Persist
        private String title;

        public NPCShopPage(int page) {
            this.index = page;
        }
    }

    @Menu(title = "NPC Shop Page Editor", type = InventoryType.CHEST, dimensions = { 5, 9 })
    public static class NPCShopPageEditor extends InventoryMenuPage {
        private MenuContext ctx;
        private final NPCShopPage page;

        public NPCShopPageEditor(NPCShopPage page) {
            this.page = page;
        }

        @ClickHandler(slot = { 0, 4 })
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

        @ClickHandler(slot = { 4, 3 })
        @MenuSlot(slot = { 4, 4 }, material = Material.TNT, amount = 1, title = "<c>Remove page")
        public void removePage(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.data().put("removePage", page.index);
            ctx.getMenu().transitionBack();
        }
    }

    public enum ShopType {
        BUY,
        COMMAND,
        SELL;
    }

    @Persist(value = "npcShops", reify = true, namespace = "shopstrait")
    private static Map<String, NPCShop> NPC_SHOPS = Maps.newHashMap();
    @Persist(value = "namedShops", reify = true, namespace = "shopstrait")
    private static Map<String, NPCShop> SHOPS = Maps.newHashMap();
}