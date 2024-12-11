package net.citizensnpcs.trait;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
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
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.Persistable;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitEventHandler;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.Placeholders;
import net.citizensnpcs.trait.shop.CommandAction;
import net.citizensnpcs.trait.shop.CommandAction.CommandActionGUI;
import net.citizensnpcs.trait.shop.ExperienceAction;
import net.citizensnpcs.trait.shop.ExperienceAction.ExperienceActionGUI;
import net.citizensnpcs.trait.shop.ItemAction;
import net.citizensnpcs.trait.shop.ItemAction.ItemActionGUI;
import net.citizensnpcs.trait.shop.MoneyAction;
import net.citizensnpcs.trait.shop.MoneyAction.MoneyActionGUI;
import net.citizensnpcs.trait.shop.NPCShopAction;
import net.citizensnpcs.trait.shop.NPCShopAction.GUI;
import net.citizensnpcs.trait.shop.NPCShopAction.Transaction;
import net.citizensnpcs.trait.shop.OpenShopAction;
import net.citizensnpcs.trait.shop.OpenShopAction.OpenShopActionGUI;
import net.citizensnpcs.trait.shop.PermissionAction;
import net.citizensnpcs.trait.shop.PermissionAction.PermissionActionGUI;
import net.citizensnpcs.trait.shop.StoredShops;
import net.citizensnpcs.util.InventoryMultiplexer;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;

/**
 * Shop trait for NPC GUI shops.
 */
@TraitName("shop")
public class ShopTrait extends Trait {
    @Persist
    private String rightClickShop;
    private StoredShops shops;

    public ShopTrait() {
        super("shop");
    }

    public ShopTrait(StoredShops shops) {
        this();
        this.shops = shops;
    }

    public NPCShop getDefaultShop() {
        return shops.npcShops.computeIfAbsent(npc.getUniqueId().toString(), NPCShop::new);
    }

    public NPCShop getShop(String name) {
        return shops.globalShops.computeIfAbsent(name, NPCShop::new);
    }

    @Override
    public void onRemove() {
        shops.deleteShop(getDefaultShop());
    }

    @TraitEventHandler(@EventHandler)
    public void onRightClick(NPCRightClickEvent event) {
        if (rightClickShop == null || rightClickShop.isEmpty())
            return;
        Player player = event.getClicker();
        event.setDelayedCancellation(true);

        String globalViewPermission = Setting.SHOP_GLOBAL_VIEW_PERMISSION.asString();
        if (!globalViewPermission.isEmpty() && !player.hasPermission(globalViewPermission))
            return;

        NPCShop shop = shops.globalShops.getOrDefault(rightClickShop, getDefaultShop());
        shop.display(player);
    }

    public void setDefaultShop(NPCShop shop) {
        shops.npcShops.put(npc.getUniqueId().toString(), shop);
    }

    public static class NPCShop {
        @Persist(value = "")
        private String name;
        @Persist(reify = true)
        private final List<NPCShopPage> pages = Lists.newArrayList();
        @Persist
        private String title;
        @Persist
        private ShopType type = ShopType.DEFAULT;
        @Persist
        private String viewPermission;

        private NPCShop() {
        }

        public NPCShop(String name) {
            this.name = name;
        }

        public boolean canEdit(NPC npc, Player sender) {
            return sender.hasPermission("citizens.admin") || sender.hasPermission("citizens.npc.shop.edit")
                    || sender.hasPermission("citizens.npc.shop.edit." + getName())
                    || (npc != null && npc.getOrAddTrait(Owner.class).isOwnedBy(sender));
        }

        public boolean canView(Player sender) {
            if (viewPermission != null && !sender.hasPermission(viewPermission))
                return false;
            return Setting.SHOP_GLOBAL_VIEW_PERMISSION.asString().isEmpty()
                    || sender.hasPermission(Setting.SHOP_GLOBAL_VIEW_PERMISSION.asString());
        }

        public void display(Player sender) {
            if (viewPermission != null && !sender.hasPermission(viewPermission)
                    || !Setting.SHOP_GLOBAL_VIEW_PERMISSION.asString().isEmpty()
                            && !sender.hasPermission(Setting.SHOP_GLOBAL_VIEW_PERMISSION.asString()))
                return;

            if (pages.size() == 0) {
                Messaging.sendError(sender, "Empty shop");
                return;
            }
            if (type == ShopType.TRADER) {
                CitizensAPI.registerEvents(new NPCTraderShopViewer(this, sender));
            } else {
                InventoryMenu.createSelfRegistered(new NPCShopViewer(this, sender)).present(sender);
            }
        }

        public void displayEditor(ShopTrait trait, Player sender) {
            InventoryMenu.createSelfRegistered(new NPCShopSettings(trait, this)).present(sender);
        }

        public String getName() {
            return name == null ? "" : name;
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

        public ShopType getShopType() {
            return type == null ? type = ShopType.DEFAULT : type;
        }

        public String getTitle() {
            return title == null ? "" : title;
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
            viewPermission = permission;
            if (viewPermission != null && viewPermission.isEmpty()) {
                viewPermission = null;
            }
        }

        public void setShopType(ShopType type) {
            this.type = type;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    @Menu(title = "NPC Shop Contents Editor", type = InventoryType.CHEST, dimensions = { 5, 9 })
    public static class NPCShopContentsEditor extends InventoryMenuPage {
        private NPCShopItem copying;
        private MenuContext ctx;
        private int page = 0;
        private final NPCShop shop;

        public NPCShopContentsEditor(NPCShop shop) {
            this.shop = shop;
        }

        public void changePage(int newPage) {
            page = newPage;
            ctx.setTitle("NPC Shop Contents Editor (" + (newPage + 1) + "/" + (shop.pages.size() + 1) + ")");
            NPCShopPage shopPage = shop.getOrCreatePage(page);
            for (int i = 0; i < ctx.getInventory().getSize(); i++) {
                InventoryMenuSlot slot = ctx.getSlot(i);
                slot.clear();

                if (shopPage.getItem(i) != null) {
                    slot.setItemStack(shopPage.getItem(i).getDisplayItem(null));
                }
                int idx = i;
                slot.setClickHandler(evt -> {
                    NPCShopItem display = shopPage.getItem(idx);
                    if (display != null && evt.isShiftClick() && evt.getCursorNonNull().getType() == Material.AIR
                            && display.display != null) {
                        copying = display.clone();
                        if (!evt.isRightClick()) {
                            shopPage.setItem(idx, null);
                            slot.setItemStack(null);
                        }
                        evt.setCursor(display.getDisplayItem(null));
                        evt.setCancelled(true);
                        return;
                    }
                    if (display == null) {
                        if (copying != null && evt.getCursorNonNull().getType() != Material.AIR
                                && evt.getCursorNonNull().equals(copying.getDisplayItem(null))) {
                            shopPage.setItem(idx, copying);
                            slot.setItemStack(copying.getDisplayItem(null));
                            copying = null;
                            return;
                        }
                        display = new NPCShopItem();
                        if (evt.getCursor() != null) {
                            display.display = evt.getCursor().clone();
                        }
                    }
                    ctx.clearSlots();
                    ctx.getMenu().transition(new NPCShopItemEditor(display, modified -> {
                        if (modified == null) {
                            shopPage.removeItem(idx);
                        } else {
                            shopPage.setItem(idx, modified);
                        }
                    }));
                });
            }
            InventoryMenuSlot prev = ctx.getSlot(shop.getShopType().prevSlotIndex);
            InventoryMenuSlot edit = ctx.getSlot(shop.getShopType().editSlotIndex);
            InventoryMenuSlot next = ctx.getSlot(shop.getShopType().nextSlotIndex);
            if (page > 0) {
                prev.setItemStack(shopPage.getNextPageItem(null, shop.getShopType().prevSlotIndex),
                        "Previous page (" + newPage + ")");
                Consumer<CitizensInventoryClickEvent> prevItemEditor = prev.getClickHandlers().get(0);
                prev.setClickHandler(evt -> {
                    if (evt.isShiftClick()) {
                        prevItemEditor.accept(evt);
                        return;
                    }
                    evt.setCancelled(true);
                    changePage(page - 1);
                });
            }
            next.setItemStack(shopPage.getNextPageItem(null, shop.getShopType().nextSlotIndex),
                    page + 1 >= shop.pages.size() ? "New page" : "Next page (" + (newPage + 1) + ")");
            Consumer<CitizensInventoryClickEvent> nextItemEditor = next.getClickHandlers().get(0);
            next.setClickHandler(evt -> {
                if (evt.isShiftClick()) {
                    nextItemEditor.accept(evt);
                    return;
                }
                evt.setCancelled(true);
                changePage(page + 1);
            });

            Consumer<CitizensInventoryClickEvent> editPageItem = edit.getClickHandlers().get(0);
            edit.setItemStack(new ItemStack(Material.BOOK), "Edit page");
            edit.setClickHandler(evt -> {
                if (evt.isShiftClick()) {
                    editPageItem.accept(evt);
                    return;
                }
                ctx.getMenu().transition(new NPCShopPageSettings(shop.getOrCreatePage(page)));
            });
        }

        @Override
        public Inventory createInventory(String title) {
            return Bukkit.createInventory(null, shop.getShopType().inventorySize, "NPC Shop Contents Editor");
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

    public static class NPCShopItem implements Cloneable, Persistable {
        @Persist
        private String alreadyPurchasedMessage;
        @Persist
        private String clickToConfirmMessage;
        @Persist
        private List<NPCShopAction> cost = Lists.newArrayList();
        @Persist
        private String costMessage;
        @Persist
        private ItemStack display;
        @Persist
        private boolean maxRepeatsOnShiftClick;
        @Persist(keyType = UUID.class)
        private final Map<UUID, Integer> purchases = Maps.newHashMap();
        @Persist
        private List<NPCShopAction> result = Lists.newArrayList();
        @Persist
        private String resultMessage;
        @Persist
        private int timesPurchasable = 0;

        private List<Transaction> apply(List<NPCShopAction> actions, Function<NPCShopAction, Transaction> func) {
            List<Transaction> pending = Lists.newArrayList();
            for (NPCShopAction action : actions) {
                Transaction take = func.apply(action);
                if (!take.isPossible()) {
                    pending.forEach(Transaction::rollback);
                    return null;
                } else {
                    take.run();
                    pending.add(take);
                }
            }
            return pending;
        }

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

        private void changeCost(Function<NPCShopAction, Boolean> filter, NPCShopAction cost) {
            changeAction(this.cost, filter, cost);
        }

        private void changeResult(Function<NPCShopAction, Boolean> filter, NPCShopAction result) {
            changeAction(this.result, filter, result);
        }

        @Override
        public NPCShopItem clone() {
            try {
                NPCShopItem dup = (NPCShopItem) super.clone();
                dup.cost = Lists.newArrayList();
                for (NPCShopAction src : cost) {
                    dup.cost.add(src.clone());
                }
                dup.result = Lists.newArrayList();
                for (NPCShopAction src : result) {
                    dup.result.add(src.clone());
                }
                return dup;
            } catch (CloneNotSupportedException e) {
                throw new Error(e);
            }
        }

        public List<NPCShopAction> getCost() {
            return cost;
        }

        public ItemStack getDisplayItem(Player player) {
            if (display == null)
                return null;
            ItemStack stack = display.clone();
            ItemMeta meta = stack.getItemMeta();
            if (meta.hasDisplayName()) {
                meta.setDisplayName(placeholders(meta.getDisplayName(), player));
            }
            if (!meta.hasLore()) {
                List<String> lore = Lists.newArrayList();
                cost.forEach(c -> lore.add(c.describe()));
                result.forEach(r -> {
                    if (!(r instanceof CommandAction)) {
                        lore.add(r.describe());
                    }
                });

                if (timesPurchasable > 0) {
                    lore.add("Times purchasable: " + timesPurchasable);
                }
                meta.setLore(lore);
            }
            if (meta.hasLore()) {
                meta.setLore(Lists.transform(meta.getLore(), line -> placeholders(line, player)));
            }
            stack.setItemMeta(meta);
            return stack;
        }

        public List<NPCShopAction> getResult() {
            return result;
        }

        @Override
        public void load(DataKey key) {
            if (key.keyExists("message")) {
                resultMessage = key.getString("message");
                key.removeKey("message");
            }
            if (key.keyExists("clickMessage")) {
                resultMessage = key.getString("clickMessage");
                key.removeKey("clickMessage");
            }
        }

        private void onClick(NPCShop shop, Player player, InventoryMultiplexer inventory, boolean shiftClick,
                boolean secondClick) {
            // TODO: InventoryMultiplexer could be lifted up to transact in apply(), which would be cleaner.
            // if this is done, it should probably refresh after every transaction application
            if (timesPurchasable > 0 && purchases.getOrDefault(player.getUniqueId(), 0) == timesPurchasable) {
                if (alreadyPurchasedMessage != null) {
                    Messaging.sendColorless(player, placeholders(alreadyPurchasedMessage, player));
                }
                return;
            }
            if (clickToConfirmMessage != null && !secondClick) {
                Messaging.sendColorless(player, placeholders(clickToConfirmMessage, player));
                return;
            }
            int max = Integer.MAX_VALUE;
            if (maxRepeatsOnShiftClick && shiftClick) {
                for (NPCShopAction action : cost) {
                    int r = action.getMaxRepeats(player, inventory);
                    if (r != -1) {
                        max = Math.min(max, r);
                    }
                }
                if (max == 0)
                    return;
            }
            int repeats = max == Integer.MAX_VALUE ? 1 : max;
            List<Transaction> take = apply(cost, action -> action.take(player, inventory, repeats));
            if (take == null) {
                if (costMessage != null) {
                    Messaging.sendColorless(player, placeholders(costMessage, player));
                }
                return;
            }
            if (apply(result, action -> action.grant(player, inventory, repeats)) == null) {
                take.forEach(Transaction::rollback);
                return;
            }
            if (resultMessage != null) {
                Messaging.sendColorless(player, placeholders(resultMessage, player));
            }
            if (timesPurchasable > 0) {
                purchases.put(player.getUniqueId(), purchases.getOrDefault(player.getUniqueId(), 0) + 1);
            }
            if (NPCShopPurchaseEvent.HANDLERS.getRegisteredListeners().length > 0) {
                Bukkit.getPluginManager().callEvent(new NPCShopPurchaseEvent(player, shop, this));
            }
        }

        private String placeholders(String string, Player player) {
            string = Placeholders.replace(string, player);
            StringBuffer sb = new StringBuffer();
            Matcher matcher = PLACEHOLDER_REGEX.matcher(string);
            while (matcher.find()) {
                matcher.appendReplacement(sb,
                        Joiner.on(", ")
                                .join(Iterables.transform(matcher.group(1).equalsIgnoreCase("cost") ? cost : result,
                                        NPCShopAction::describe))
                                .replace("$", "\\$").replace("{", "\\{"));
            }
            matcher.appendTail(sb);
            return sb.toString();
        }

        @Override
        public void save(DataKey key) {
        }

        private static final Pattern PLACEHOLDER_REGEX = Pattern.compile("<(cost|result)>", Pattern.CASE_INSENSITIVE);
    }

    @Menu(title = "NPC Shop Item Editor", type = InventoryType.CHEST, dimensions = { 6, 9 })
    public static class NPCShopItemEditor extends InventoryMenuPage {
        @MenuPattern(
                offset = { 0, 6 },
                slots = { @MenuSlot(pat = 'x', material = Material.AIR) },
                value = "xxx\nxxx\nxxx")
        private InventoryMenuPattern actionItems;
        private NPCShopItem base;
        private final Consumer<NPCShopItem> callback;
        @MenuPattern(
                offset = { 0, 0 },
                slots = { @MenuSlot(pat = 'x', material = Material.AIR) },
                value = "xxx\nxxx\nxxx")
        private InventoryMenuPattern costItems;
        private MenuContext ctx;
        private final NPCShopItem modified;

        public NPCShopItemEditor(NPCShopItem item, Consumer<NPCShopItem> consumer) {
            base = item;
            modified = base.clone();
            callback = consumer;
        }

        private ItemStack editTitle(ItemStack item, Function<String, String> transform) {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(transform.apply(meta.hasDisplayName() ? meta.getDisplayName() : ""));
            item.setItemMeta(meta);
            return item;
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.ctx = ctx;
            if (modified.display != null) {
                ctx.getSlot(9 * 4 + 4).setItemStack(modified.getDisplayItem(null));
            }
            ctx.getSlot(9 * 3 + 2).setItemStack(new ItemStack(Material.EGG), "Only purchasable once per player",
                    "Times purchasable: " + modified.timesPurchasable
                            + (modified.timesPurchasable == 0 ? " (no limit)" : ""));
            ctx.getSlot(9 * 3 + 2).setClickHandler(e -> ctx.getMenu()
                    .transition(InputMenus.stringSetter(() -> String.valueOf(modified.timesPurchasable), s -> {
                        modified.timesPurchasable = Integer.parseInt(s);
                        ctx.getSlot(9 * 4 + 2).setDescription("Times purchasable: " + modified.timesPurchasable
                                + (modified.timesPurchasable == 0 ? " (no limit)" : ""));
                    })));

            ctx.getSlot(9 * 4 + 2).setItemStack(new ItemStack(Util.getFallbackMaterial("OAK_SIGN", "SIGN")),
                    "Set already purchased message, currently:\n",
                    modified.alreadyPurchasedMessage == null ? "Unset" : modified.alreadyPurchasedMessage);
            ctx.getSlot(9 * 4 + 2).setClickHandler(e -> InputMenus.runChatStringSetter(ctx.getMenu(), e,
                    "Enter the new already purchased message, currently:<br>[[" + modified.alreadyPurchasedMessage,
                    s -> {
                        modified.alreadyPurchasedMessage = s;
                        ctx.getSlot(9 * 4 + 2).setDescription(modified.alreadyPurchasedMessage);
                    }));

            ctx.getSlot(9 * 3 + 3).setItemStack(
                    new ItemStack(Util.getFallbackMaterial("GREEN_WOOL", "EMERALD", "OAK_SIGN", "SIGN")),
                    "Set successful click message, currently:\n",
                    modified.resultMessage == null ? "Unset" : modified.resultMessage);
            ctx.getSlot(9 * 3 + 3).setClickHandler(e -> InputMenus.runChatStringSetter(ctx.getMenu(), e,
                    "Enter the new successful click message, currently:<br>[[" + modified.resultMessage, s -> {
                        modified.resultMessage = s;
                        ctx.getSlot(9 * 3 + 3).setDescription(modified.resultMessage);
                    }));

            ctx.getSlot(9 * 3 + 6).setItemStack(new ItemStack(Util.getFallbackMaterial("RED_WOOL", "OAK_SIGN", "SIGN")),
                    "Set unsuccessful click message, currently:\n",
                    modified.costMessage == null ? "Unset" : modified.costMessage);
            ctx.getSlot(9 * 3 + 6).setClickHandler(e -> InputMenus.runChatStringSetter(ctx.getMenu(), e,
                    "Enter the new unsuccessful click message, currently:<br>[[" + modified.costMessage, s -> {
                        modified.costMessage = s;
                        ctx.getSlot(9 * 3 + 6).setDescription(modified.costMessage);
                    }));

            ctx.getSlot(9 * 3 + 5).setItemStack(new ItemStack(Util.getFallbackMaterial("FEATHER", "OAK_SIGN", "SIGN")),
                    "Set click to confirm message.",
                    "For example, 'click again to buy this item'\nYou can use <cost> or <result> placeholders.\nCurrently:\n"
                            + (modified.clickToConfirmMessage == null ? "Unset" : modified.clickToConfirmMessage));
            ctx.getSlot(9 * 3 + 5).setClickHandler(e -> InputMenus.runChatStringSetter(ctx.getMenu(), e,
                    "Enter the new click to confirm message, currently:<br>[[" + modified.clickToConfirmMessage, s -> {
                        modified.clickToConfirmMessage = s;
                        ctx.getSlot(9 * 3 + 5).setDescription(modified.clickToConfirmMessage);
                    }));

            ctx.getSlot(9 * 3 + 4).setItemStack(new ItemStack(Material.REDSTONE),
                    "Sell as many times as possible on shift click\n", "Currently: " + modified.maxRepeatsOnShiftClick);
            ctx.getSlot(9 * 3 + 4).setClickHandler(
                    InputMenus.toggler(res -> modified.maxRepeatsOnShiftClick = res, modified.maxRepeatsOnShiftClick));
            int pos = 0;

            for (GUI template : NPCShopAction.getGUIs()) {
                if (template.createMenuItem(null) == null)
                    continue;

                NPCShopAction oldCost = modified.cost.stream().filter(template::manages).findFirst().orElse(null);
                costItems.getSlots().get(pos)
                        .setItemStack(editTitle(template.createMenuItem(oldCost), title -> title + " Cost"));
                costItems.getSlots().get(pos).setClickHandler(event -> ctx.getMenu().transition(
                        template.createEditor(oldCost, cost -> modified.changeCost(template::manages, cost))));

                NPCShopAction oldResult = modified.result.stream().filter(template::manages).findFirst().orElse(null);
                actionItems.getSlots().get(pos)
                        .setItemStack(editTitle(template.createMenuItem(oldResult), title -> title + " Result"));
                actionItems.getSlots().get(pos).setClickHandler(event -> ctx.getMenu().transition(
                        template.createEditor(oldResult, result -> modified.changeResult(template::manages, result))));

                pos++;
            }
        }

        @MenuSlot(slot = { 5, 3 }, material = Material.REDSTONE_BLOCK, amount = 1, title = "<7>Cancel")
        public void onCancel(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            ctx.getMenu().transitionBack();
        }

        @Override
        public void onClose(HumanEntity who) {
            if (base != null && base.display == null) {
                base = null;
            }
            callback.accept(base);
        }

        @MenuSlot(slot = { 4, 5 }, material = Material.BOOK, amount = 1, title = "<f>Set description")
        public void onEditDescription(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            if (modified.display == null)
                return;

            InputMenus.runChatStringSetter(ctx.getMenu(), event,
                    "Enter the new item description, currently:<br>[[" + (modified.display.getItemMeta().hasLore()
                            ? Joiner.on("<br>").skipNulls().join(modified.display.getItemMeta().getLore())
                            : "Unset"),
                    description -> {
                        ItemMeta meta = modified.display.getItemMeta();
                        if (description.isEmpty()) {
                            meta.setLore(Lists.newArrayList());
                        } else {
                            meta.setLore(Lists
                                    .newArrayList(Splitter.on('\n').split(Messaging.parseComponents(description))));
                        }
                        modified.display.setItemMeta(meta);
                    });
        }

        @MenuSlot(slot = { 4, 3 }, material = Material.NAME_TAG, amount = 1, title = "<f>Set name")
        public void onEditName(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            if (modified.display == null)
                return;

            ctx.getMenu().transition(InputMenus.stringSetter(modified.display.getItemMeta()::getDisplayName, name -> {
                ItemMeta meta = modified.display.getItemMeta();
                meta.setDisplayName(ChatColor.RESET + Messaging.parseComponents(name));
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
            base = null;
            ctx.getMenu().transitionBack();
        }

        @MenuSlot(slot = { 5, 5 }, material = Material.EMERALD_BLOCK, amount = 1, title = "<a>Save")
        public void onSave(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            base = modified;
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
            index = page;
        }

        public NPCShopItem getItem(int idx) {
            return items.get(idx);
        }

        public ItemStack getNextPageItem(Player player, int idx) {
            return items.containsKey(idx) ? items.get(idx).getDisplayItem(player) : new ItemStack(Material.FEATHER, 1);
        }

        public ItemStack getPreviousPageItem(Player player, int idx) {
            return items.containsKey(idx) ? items.get(idx).getDisplayItem(player) : new ItemStack(Material.FEATHER, 1);
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
            ctx.getMenu().transition(InputMenus.stringSetter(() -> page.title,
                    newTitle -> page.title = newTitle.isEmpty() ? null : newTitle));
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.ctx = ctx;
            ctx.getSlot(4).setDescription("Set page title<br>Currently: " + page.title);
        }

        @MenuSlot(slot = { 4, 4 }, material = Material.TNT, amount = 1, title = "<c>Remove page")
        public void removePage(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            ctx.data().put("removePage", page.index);
            ctx.getMenu().transitionBack();
        }
    }

    public static class NPCShopPurchaseEvent extends Event {
        private final NPCShopItem item;
        private final Player player;
        private final NPCShop shop;

        public NPCShopPurchaseEvent(Player player, NPCShop shop, NPCShopItem item) {
            this.player = player;
            this.shop = shop;
            this.item = item;
        }

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public NPCShopItem getItem() {
            return item;
        }

        public Player getPlayer() {
            return player;
        }

        public NPCShop getShop() {
            return shop;
        }

        private static final HandlerList HANDLERS = new HandlerList();
    }

    @Menu(title = "NPC Shop Editor", type = InventoryType.CHEST, dimensions = { 1, 9 })
    public static class NPCShopSettings extends InventoryMenuPage {
        private MenuContext ctx;
        private final NPCShop shop;
        private final ShopTrait trait;

        public NPCShopSettings(ShopTrait trait, NPCShop shop) {
            this.trait = trait;
            this.shop = shop;
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.ctx = ctx;
            ctx.getSlot(0)
                    .setDescription("<f>Edit permission required to view shop<br>" + shop.getRequiredPermission());
            ctx.getSlot(4).setDescription("<f>Edit shop title<br>" + shop.getTitle());
            if (trait != null) {
                ctx.getSlot(6).setDescription(
                        "<f>Show shop on right click<br>" + shop.getName().equals(trait.rightClickShop));
            }
        }

        @MenuSlot(slot = { 0, 2 }, material = Material.FEATHER, amount = 1, title = "<f>Edit shop items")
        public void onEditItems(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            ctx.getMenu().transition(new NPCShopContentsEditor(shop));
        }

        @MenuSlot(slot = { 0, 0 }, compatMaterial = { "OAK_SIGN", "SIGN" }, amount = 1)
        public void onPermissionChange(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            ctx.getMenu().transition(InputMenus.stringSetter(shop::getRequiredPermission, shop::setPermission));
        }

        @MenuSlot(slot = { 0, 8 }, material = Material.CHEST, amount = 1, title = "<f>Set shop type")
        public void onSetInventoryType(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            ctx.getMenu().transition(InputMenus.picker("Set shop type",
                    (Choice<ShopType> choice) -> shop.setShopType(choice.getValue()),
                    Choice.of(ShopType.DEFAULT, Material.CHEST, "Default (5x9 chest)",
                            shop.getShopType() == ShopType.DEFAULT),
                    Choice.of(ShopType.CHEST_4X9, Material.CHEST, "4x9 chest",
                            shop.getShopType() == ShopType.CHEST_4X9),
                    Choice.of(ShopType.CHEST_3X9, Material.CHEST, "3x9 chest",
                            shop.getShopType() == ShopType.CHEST_3X9),
                    Choice.of(ShopType.CHEST_2X9, Material.CHEST, "2x9 chest",
                            shop.getShopType() == ShopType.CHEST_2X9),
                    Choice.of(ShopType.CHEST_1X9, Material.CHEST, "1x9 chest",
                            shop.getShopType() == ShopType.CHEST_1X9),
                    Choice.of(ShopType.TRADER, Material.EMERALD, "Trader", shop.getShopType() == ShopType.TRADER)));
        }

        @MenuSlot(slot = { 0, 4 }, material = Material.NAME_TAG, amount = 1)
        public void onSetTitle(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            ctx.getMenu().transition(InputMenus.stringSetter(shop::getTitle, shop::setTitle));
        }

        @MenuSlot(slot = { 0, 6 }, compatMaterial = { "COMMAND_BLOCK", "COMMAND" }, amount = 1)
        public void onToggleRightClick(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
            event.setCancelled(true);
            if (trait == null)
                return;

            if (shop.getName().equals(trait.rightClickShop)) {
                trait.rightClickShop = null;
            } else {
                trait.rightClickShop = shop.name;
            }
            ctx.getSlot(6)
                    .setDescription("<f>Show shop on right click<br>" + shop.getName().equals(trait.rightClickShop));
        }
    }

    @Menu(title = "Shop", type = InventoryType.CHEST, dimensions = { 5, 9 })
    public static class NPCShopViewer extends InventoryMenuPage {
        private MenuContext ctx;
        private int currentPage = 0;
        private NPCShopItem lastClickedItem;
        private final Player player;
        private final NPCShop shop;

        public NPCShopViewer(NPCShop shop, Player player) {
            this.shop = shop;
            this.player = player;
        }

        public void changePage(int newPage) {
            currentPage = newPage;
            NPCShopPage page = shop.pages.get(currentPage);
            if (page.title != null && !page.title.isEmpty()) {
                Bukkit.getScheduler().runTaskLater(CitizensAPI.getPlugin(),
                        () -> ctx.setTitle(Placeholders.replace(page.title, player)), 1);
            }
            for (int i = 0; i < ctx.getInventory().getSize(); i++) {
                ctx.getSlot(i).clear();
                NPCShopItem item = page.getItem(i);
                if (item == null)
                    continue;

                ctx.getSlot(i).setItemStack(item.getDisplayItem(player));
                ctx.getSlot(i).setClickHandler(evt -> {
                    evt.setCancelled(true);
                    item.onClick(shop, (Player) evt.getWhoClicked(),
                            new InventoryMultiplexer(evt.getWhoClicked().getInventory()), evt.isShiftClick(),
                            lastClickedItem == item);
                    lastClickedItem = item;
                });
            }
            InventoryMenuSlot prev = ctx.getSlot(shop.getShopType().prevSlotIndex);
            InventoryMenuSlot next = ctx.getSlot(shop.getShopType().nextSlotIndex);
            if (currentPage > 0) {
                prev.clear();
                prev.setItemStack(page.getPreviousPageItem(player, shop.getShopType().prevSlotIndex),
                        "Previous page (" + newPage + ")");
                prev.setClickHandler(evt -> {
                    evt.setCancelled(true);
                    changePage(currentPage - 1);
                });
            }
            if (currentPage + 1 < shop.pages.size()) {
                next.clear();
                next.setItemStack(page.getNextPageItem(player, shop.getShopType().nextSlotIndex),
                        "Next page (" + (newPage + 1) + ")");
                next.setClickHandler(evt -> {
                    evt.setCancelled(true);
                    changePage(currentPage + 1);
                });
            }
        }

        @Override
        public Inventory createInventory(String title) {
            return Bukkit.createInventory(null, shop.getShopType().inventorySize, shop.getTitle().isEmpty() ? "Shop"
                    : Messaging.parseComponents(Placeholders.replace(shop.getTitle(), player)));
        }

        @Override
        public void initialise(MenuContext ctx) {
            this.ctx = ctx;
            changePage(currentPage);
        }
    }

    public static class NPCTraderShopViewer implements Listener {
        private int lastClickedTrade = -1;
        private final Player player;
        private int selectedTrade = -1;
        private final NPCShop shop;
        private final Map<Integer, NPCShopItem> trades;
        private final Object view;

        public NPCTraderShopViewer(NPCShop shop, Player player) {
            this.shop = shop;
            this.player = player;
            Map<Integer, NPCShopItem> tradesMap = Maps.newHashMap();
            Merchant merchant = Bukkit.createMerchant(shop.getTitle());
            List<MerchantRecipe> recipes = Lists.newArrayList();
            for (NPCShopPage page : shop.pages) {
                for (NPCShopItem item : page.items.values()) {
                    ItemStack result = item.getDisplayItem(player);
                    if (result == null)
                        continue;
                    MerchantRecipe recipe = new MerchantRecipe(result.clone(), 100000000);
                    for (NPCShopAction action : item.cost) {
                        if (!(action instanceof ItemAction))
                            continue;
                        ItemAction ia = (ItemAction) action;
                        for (ItemStack stack : ia.items) {
                            stack = stack.clone();
                            if (!ia.compareSimilarity && ia.metaFilter.size() > 0) {
                                // Minecraft implements its own trade selection logic on the client
                                // Clear the custom component part of the itemstack since we will check it later anyway
                                ItemMeta im = stack.getItemMeta();
                                for (NamespacedKey nk : Lists.newArrayList(im.getPersistentDataContainer().getKeys())) {
                                    im.getPersistentDataContainer().remove(nk);
                                }
                                NMS.clearCustomNBT(im);
                                stack.setItemMeta(im);
                            }
                            recipe.addIngredient(stack);
                            if (recipe.getIngredients().size() == 2)
                                break;
                        }
                    }
                    if (recipe.getIngredients().size() == 0)
                        continue;
                    tradesMap.put(recipes.size(), item);
                    recipes.add(recipe);
                }
            }
            merchant.setRecipes(recipes);
            trades = tradesMap;
            view = player.openMerchant(merchant, true);
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent evt) {
            if (!evt.getView().equals(view) || evt.getSlotType() != SlotType.RESULT)
                return;
            evt.setCancelled(true);
            if (selectedTrade == -1)
                return;
            if (!trades.containsKey(selectedTrade)) {
                Messaging.severe("Invalid trade selection", selectedTrade, "from", evt.getWhoClicked());
                return;
            }
            if (!evt.getAction().name().contains("PICKUP")
                    || (evt.getCursor() != null && evt.getCursor().getType() != Material.AIR))
                return;
            Inventory syntheticInventory = Bukkit.createInventory(null, 9);
            syntheticInventory.setItem(0, evt.getClickedInventory().getItem(0));
            syntheticInventory.setItem(1, evt.getClickedInventory().getItem(1));
            InventoryMultiplexer multiplexer = new InventoryMultiplexer(player.getInventory(), syntheticInventory);
            trades.get(selectedTrade).onClick(shop, player, multiplexer, evt.getClick().isShiftClick(),
                    lastClickedTrade == selectedTrade);
            evt.getClickedInventory().setItem(0, syntheticInventory.getItem(0));
            evt.getClickedInventory().setItem(1, syntheticInventory.getItem(1));
            lastClickedTrade = selectedTrade;
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent evt) {
            if (!evt.getPlayer().equals(player))
                return;
            HandlerList.unregisterAll(this);
        }

        @EventHandler
        public void onTradeSelect(TradeSelectEvent evt) {
            if (!evt.getView().equals(view))
                return;
            selectedTrade = evt.getIndex();
            lastClickedTrade = -1;
        }
    }

    public enum ShopType {
        CHEST_1X9(1 * 9, 7, 6, 8),
        CHEST_2X9(2 * 9),
        CHEST_3X9(3 * 9),
        CHEST_4X9(4 * 9),
        DEFAULT(5 * 9),
        TRADER(5 * 9);

        private final int editSlotIndex;
        private final int inventorySize;
        private final int nextSlotIndex;
        private final int prevSlotIndex;

        ShopType(int inventorySize) {
            this(inventorySize, inventorySize - 9 + 3, inventorySize - 9 + 4, inventorySize - 9 + 5);
        }

        ShopType(int inventorySize, int prevSlotIndex, int editSlotIndex, int nextSlotIndex) {
            this.inventorySize = inventorySize;
            this.prevSlotIndex = prevSlotIndex;
            this.editSlotIndex = editSlotIndex;
            this.nextSlotIndex = nextSlotIndex;
        }
    }

    static {
        NPCShopAction.register(ItemAction.class, "items", new ItemActionGUI());
        NPCShopAction.register(PermissionAction.class, "permissions", new PermissionActionGUI());
        NPCShopAction.register(MoneyAction.class, "money", new MoneyActionGUI());
        NPCShopAction.register(CommandAction.class, "command", new CommandActionGUI());
        NPCShopAction.register(ExperienceAction.class, "experience", new ExperienceActionGUI());
        NPCShopAction.register(OpenShopAction.class, "open_shop", new OpenShopActionGUI());
    }
}
