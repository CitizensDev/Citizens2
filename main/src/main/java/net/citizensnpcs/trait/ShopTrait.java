package net.citizensnpcs.trait;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

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
        private ShopType type = ShopType.VIEW;
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

        public String getRequiredPermission() {
            return viewPermission;
        }

        public void setPermission(String permission) {
            this.viewPermission = permission;
            if (viewPermission != null && viewPermission.isEmpty()) {
                viewPermission = null;
            }
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
            ctx.getMenu().transition(new NPCShopItemEditor(shop));
        }

        @ClickHandler(slot = { 0, 2 })
        public void onPermissionChange(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.getMenu().transition(
                    InputMenus.stringSetter(() -> shop.getRequiredPermission(), p -> shop.setPermission(p)));
        }

        @ClickHandler(slot = { 0, 0 })
        public void onShopTypeChange(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            ctx.getMenu().transition(InputMenus.<ShopType> picker("Edit shop type", chosen -> {
                shop.type = chosen.getValue();
            }, Choice.<ShopType> of(ShopType.BUY, Material.DIAMOND, "Players buy items", shop.type == ShopType.BUY),
                    Choice.of(ShopType.SELL, Material.EMERALD, "Players sell items", shop.type == ShopType.SELL),
                    Choice.of(ShopType.VIEW, Material.ENDER_EYE, "Players view items only",
                            shop.type == ShopType.VIEW)));
        }
    }

    public static class NPCShopItem {
        @Persist
        private int cost;
        @Persist
        private ItemStack display;
    }

    @Menu(title = "NPC Shop Item Editor", type = InventoryType.CHEST, dimensions = { 5, 9 })
    public static class NPCShopItemEditor extends InventoryMenuPage {
        private MenuContext ctx;
        private final NPCShop shop;

        public NPCShopItemEditor(NPCShop shop) {
            this.shop = shop;
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.ctx = ctx;
        }
    }

    public static class NPCShopPage {
        @Persist("")
        private int index;
        @Persist(reify = true)
        private final Map<Integer, NPCShopItem> items = Maps.newHashMap();
        @Persist
        private String title;
    }

    public enum ShopType {
        BUY,
        SELL,
        VIEW;
    }

    @Persist(value = "npcShops", reify = true, namespace = "shopstrait")
    private static Map<String, NPCShop> NPC_SHOPS = Maps.newHashMap();
    @Persist(value = "namedShops", reify = true, namespace = "shopstrait")
    private static Map<String, NPCShop> SHOPS = Maps.newHashMap();
}