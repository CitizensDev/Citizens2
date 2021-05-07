package net.citizensnpcs.api.gui;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.util.Colorizer;
import net.citizensnpcs.api.util.Messaging;

// TODO: class-based injection? sub-inventory pages
/**
 * A container class for Inventory GUIs. Expects {@link #onInventoryClick(InventoryClickEvent)} and
 * {@link #onInventoryClose(InventoryCloseEvent)} to be called by the user (or registered with the event listener
 * system). Optionally, {@link #run()} can also be called every tick.
 *
 * Inventory GUIs are defined as a stack of {@link InventoryMenuPage}s, each of which represents a distinct inventory
 * that is transitioned between using either code or user clicks using the {@link InventoryMenuTransition} class. Each
 * {@link InventoryMenuPage} should define a {@link Menu} annotation at the class level.
 *
 * Each page has a number of {@link InventoryMenuSlot}s which define attributes such as default slot item,
 * interactibility, etc.
 *
 * You can define sets of slots and transitions using {@link InventoryMenuPattern}.
 *
 * For each concrete class of slot/transition/pattern there is a corresponding annotation that is defined.
 * {@link InventoryMenuPage}s can either annotate specific instances of these concrete classes which will be injected at
 * runtime or simply place them at the method/class level.
 *
 * Instances of global/contextual variables can be injected dynamically via {@link InjectContext} which sources
 * variables from the {@link MenuContext}.
 */
public class InventoryMenu implements Listener, Runnable {
    private final List<Runnable> closeCallbacks = Lists.newArrayList();
    private PageContext page;
    private int pickupAmount = -1;
    private final Queue<PageContext> stack = Queues.newArrayDeque();
    private Collection<InventoryView> views = Lists.newArrayList();

    public InventoryMenu(InventoryMenuInfo info, InventoryMenuPage instance) {
        transition(info, instance, Maps.newHashMap());
    }

    private InventoryMenu(InventoryMenuInfo info, Map<String, Object> context) {
        transition(info, info.createInstance(), context);
    }

    private boolean acceptFilter(InventoryAction needle, InventoryAction[] haystack) {
        for (InventoryAction type : haystack) {
            if (needle == type) {
                return true;
            }
        }
        return haystack.length == 0;
    }

    private void addCloseCallback(Runnable run) {
        closeCallbacks.add(run);
    }

    /**
     * Closes the GUI and all associated viewer inventories.
     */
    public void close() {
        HandlerList.unregisterAll(this);
        for (InventoryView view : views) {
            page.page.onClose(view.getPlayer());
            view.close();
        }
    }

    /**
     * Closes the GUI for just a specific Player.
     */
    public void close(Player player) {
        for (InventoryView view : views) {
            if (view.getPlayer() == player) {
                page.page.onClose(player);
                view.close();
            }
        }
    }

    private InventoryMenuSlot createSlot(int pos, MenuSlot slotInfo) {
        InventoryMenuSlot slot = page.ctx.getSlot(pos);
        slot.initialise(slotInfo);
        return slot;
    }

    private InventoryMenuTransition createTransition(int pos, MenuTransition transitionInfo) {
        InventoryMenuSlot slot = page.ctx.getSlot(pos);
        InventoryMenuTransition transition = new InventoryMenuTransition(slot, transitionInfo.value());
        return transition;
    }

    private int getInventorySize(InventoryType type, int[] dim) {
        switch (type) {
            case CHEST:
                int size = dim[0] * dim[1];
                if (size % 9 != 0) {
                    size += 9 - (size % 9);
                }
                dim[0] = Math.min(54, size) / 9;
                dim[1] = 9;
                return Math.max(9, Math.min(54, size));
            case ANVIL:
            case BLAST_FURNACE:
            case CARTOGRAPHY:
            case FURNACE:
            case GRINDSTONE:
            case SMITHING:
            case SMOKER:
                dim[0] = 0;
                dim[1] = 3;
                return 3;
            case BARREL:
            case ENDER_CHEST:
            case SHULKER_BOX:
                dim[0] = 3;
                dim[1] = 9;
                return 27;
            case BEACON:
            case LECTERN:
                dim[0] = 0;
                dim[1] = 1;
                return 1;
            case BREWING:
            case HOPPER:
                dim[0] = 0;
                dim[1] = 5;
                return 5;
            case DISPENSER:
            case DROPPER:
                dim[0] = 0;
                dim[1] = 9;
                return 9;
            case ENCHANTING:
            case STONECUTTER:
                dim[0] = 0;
                dim[1] = 2;
                return 2;
            case LOOM:
                dim[0] = 0;
                dim[1] = 4;
                return 4;
            case PLAYER:
                dim[0] = 4;
                dim[1] = 9;
                return 41;
            case WORKBENCH:
                dim[0] = 0;
                dim[1] = 10;
                return 10;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public void transitionBack() {
        if (page == null)
            return;
        Map<String, Object> data = page.ctx.data();
        page = stack.poll();
        if (page != null) {
            page.ctx.data().putAll(data);
        }
        data.clear();
        transitionViewersToInventory(page == null ? null : page.ctx.getInventory());
        if (page == null) {
            for (Runnable callback : closeCallbacks) {
                callback.run();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (page == null)
            return;
        Inventory clicked = event.getClickedInventory() != null ? event.getClickedInventory() : event.getInventory();
        if (event.getInventory().equals(page.ctx.getInventory())
                && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            Inventory dest = event.getInventory() == event.getClickedInventory() ? event.getWhoClicked().getInventory()
                    : event.getInventory();
            boolean toNPC = dest == page.ctx.getInventory();
            if ((event.getCursor() == null || event.getCursor().getType() == Material.AIR)) {
                int amount = event.getCurrentItem().getAmount();
                ItemStack merging = new ItemStack(event.getCurrentItem().clone());
                ItemStack[] contents = dest.getContents();
                for (int i = 0; i < contents.length; i++) {
                    if (contents[i] == null || contents[i].getType() == Material.AIR) {
                        merging.setAmount(amount);
                        if (toNPC) {
                            event.getView().setCursor(merging);
                        }
                        InventoryClickEvent e = new InventoryClickEvent(event.getView(), event.getSlotType(),
                                toNPC ? i : event.getRawSlot(), event.getClick(),
                                toNPC ? InventoryAction.PLACE_ALL : InventoryAction.PICKUP_ALL);
                        onInventoryClick(e);
                        if (toNPC) {
                            event.getView().setCursor(null);
                        }
                        if (!e.isCancelled() && e.getResult() != Result.DENY) {
                            dest.setItem(i, merging);
                            event.setCurrentItem(null);
                            break;
                        }
                    } else if (contents[i].getType() == event.getCurrentItem().getType()) {
                        ItemStack stack = contents[i].clone();
                        merging.setAmount(Math.min(amount, stack.getType().getMaxStackSize() - stack.getAmount()));
                        InventoryAction action;
                        if (toNPC) {
                            event.getView().setCursor(merging);
                            action = amount - merging.getAmount() <= 0 ? InventoryAction.PLACE_ALL
                                    : InventoryAction.PLACE_SOME;
                        } else {
                            action = amount - merging.getAmount() <= 0 ? InventoryAction.PICKUP_ALL
                                    : InventoryAction.PICKUP_SOME;
                            pickupAmount = merging.getAmount();
                        }
                        InventoryClickEvent e = new InventoryClickEvent(event.getView(), event.getSlotType(),
                                toNPC ? i : event.getRawSlot(), event.getClick(), action);
                        onInventoryClick(e);
                        if (toNPC) {
                            event.getView().setCursor(null);
                        }
                        if (!e.isCancelled() && e.getResult() != Result.DENY) {
                            stack.setAmount(stack.getAmount() + merging.getAmount());
                            dest.setItem(i, stack);
                            amount -= merging.getAmount();
                            event.getCurrentItem().setAmount(amount);
                            if (amount <= 0) {
                                break;
                            }
                        }
                    }
                }
                return;
            }
        }
        if (!clicked.equals(page.ctx.getInventory()))
            return;
        switch (event.getAction()) {
            case COLLECT_TO_CURSOR:
                event.setCancelled(true);
            case NOTHING:
            case UNKNOWN:
            case DROP_ONE_CURSOR:
            case DROP_ALL_CURSOR:
                return;
            default:
                break;
        }
        InventoryMenuSlot slot = page.ctx.getSlot(event.getSlot());
        CitizensInventoryClickEvent ev = new CitizensInventoryClickEvent(event, pickupAmount);
        slot.onClick(ev);
        pickupAmount = -1;
        if (event.isCancelled()) {
            return;
        }
        page.page.onClick(slot, event);
        for (Invokable<ClickHandler> invokable : page.clickHandlers) {
            int idx = posToIndex(page.dim, invokable.data.slot());
            if (event.getSlot() != idx)
                continue;
            if (acceptFilter(event.getAction(), invokable.data.filter())) {
                try {
                    // TODO: optional args?
                    invokable.method.invoke(page.page, slot, ev);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } else {
                event.setCancelled(true);
                event.setResult(Result.DENY);
                return;
            }
        }
        for (InventoryMenuTransition transition : page.transitions) {
            Class<? extends InventoryMenuPage> next = transition.accept(slot);
            if (next != null) {
                transition(next);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (page == null || !event.getInventory().equals(page.ctx.getInventory()))
            return;
        page.page.onClose(event.getPlayer());
        transitionBack();
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        // TODO: should this be supported
        if (page != null && event.getInventory() == page.ctx.getInventory()) {
            event.setCancelled(true);
        }
    }

    private InventoryMenuPattern parsePattern(int[] dim, List<InventoryMenuTransition> transitions,
            Bindable<MenuPattern> patternInfo) {
        String pattern = patternInfo.data.value();
        Map<Character, MenuSlot> slotMap = Maps.newHashMap();
        for (MenuSlot slot : patternInfo.data.slots()) {
            slotMap.put(slot.pat(), slot);
        }
        Map<Character, MenuTransition> transitionMap = Maps.newHashMap();
        for (MenuTransition transition : patternInfo.data.transitions()) {
            transitionMap.put(transition.pat(), transition);
        }

        Collection<InventoryMenuSlot> patternSlots = Lists.newArrayList();
        Collection<InventoryMenuTransition> patternTransitions = Lists.newArrayList();
        int row = 0;
        int col = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '\n' || (c == '\\' && i + 1 < pattern.length() && pattern.charAt(i + 1) == 'n')) {
                if (c != '\n') {
                    i++;
                }
                row++;
                col = 0;
                continue;
            }
            int[] pos = patternInfo.data.offset();
            pos[0] += row;
            pos[1] += col;

            MenuSlot slot = slotMap.get(c);
            if (slot != null) {
                patternSlots.add(createSlot(posToIndex(dim, pos), slot));
            }
            MenuTransition transition = transitionMap.get(c);
            if (transition != null) {
                InventoryMenuTransition concreteTransition = createTransition(posToIndex(dim, pos), transition);
                patternTransitions.add(concreteTransition);
                transitions.add(concreteTransition);
            }
            col++;
        }

        return new InventoryMenuPattern(patternInfo.data, patternSlots, patternTransitions);
    }

    private int posToIndex(int[] dim, int[] pos) {
        return pos[0] * dim[1] + pos[1];
    }

    /**
     * Display the menu to the given player. Multiple players can be shown the same menu, but transitions will affect
     * all players and the inventory is shared.
     */
    public void present(Player player) {
        InventoryView view = player.openInventory(page.ctx.getInventory());
        views.add(view);
    }

    @Override
    public void run() {
        page.page.run();
    }

    /**
     * Transition to another page. Adds the previous page to a stack which will be returned to when the current page is
     * closed.
     */
    public void transition(Class<? extends InventoryMenuPage> clazz) {
        transition(clazz, Maps.newHashMap());
    }

    /**
     * Transition to another page with context. Adds the previous page to a stack which will be returned to when the
     * current page is closed.
     */
    public void transition(Class<? extends InventoryMenuPage> clazz, Map<String, Object> context) {
        if (!CACHED_INFOS.containsKey(clazz)) {
            cacheInfo(clazz);
        }
        InventoryMenuInfo info = CACHED_INFOS.get(clazz);
        transition(info, info.createInstance(), context);
    }

    private void transition(InventoryMenuInfo info, InventoryMenuPage instance, Map<String, Object> context) {
        if (page != null) {
            context.putAll(page.ctx.data());
            page.ctx.data().clear();
            stack.add(page);
        }
        page = new PageContext();
        int[] dim = info.menuAnnotation.dimensions();
        InventoryType type = info.menuAnnotation.type();
        page.page = instance;
        if (instance.getInventoryType() != null) {
            type = instance.getInventoryType();
        }
        int size = getInventorySize(type, dim);
        Inventory inventory;
        if (type == InventoryType.CHEST || type == null) {
            inventory = Bukkit.createInventory(null, size,
                    Colorizer.parseColors(Messaging.tryTranslate(info.menuAnnotation.title())));
        } else {
            inventory = Bukkit.createInventory(null, type,
                    Colorizer.parseColors(Messaging.tryTranslate(info.menuAnnotation.title())));
        }
        List<InventoryMenuTransition> transitions = Lists.newArrayList();
        InventoryMenuSlot[] slots = new InventoryMenuSlot[inventory.getSize()];
        page.patterns = new InventoryMenuPattern[info.patterns.length];
        page.dim = dim;
        page.ctx = new MenuContext(this, slots, inventory, context);
        for (int i = 0; i < info.slots.length; i++) {
            Bindable<MenuSlot> slotInfo = info.slots[i];
            int pos = posToIndex(dim, slotInfo.data.slot());
            InventoryMenuSlot slot = createSlot(pos, slotInfo.data);
            slotInfo.bind(page.page, slot);
        }
        for (int i = 0; i < info.transitions.length; i++) {
            Bindable<MenuTransition> transitionInfo = info.transitions[i];
            int pos = posToIndex(dim, transitionInfo.data.pos());
            InventoryMenuTransition transition = createTransition(pos, transitionInfo.data);
            transitionInfo.bind(page.page, transition);
            transitions.add(transition);
        }
        for (int i = 0; i < info.patterns.length; i++) {
            Bindable<MenuPattern> patternInfo = info.patterns[i];
            InventoryMenuPattern pattern = parsePattern(dim, transitions, patternInfo);
            patternInfo.bind(page.page, pattern);
            page.patterns[i] = pattern;
        }
        page.transitions = transitions.toArray(new InventoryMenuTransition[transitions.size()]);
        page.clickHandlers = info.clickHandlers;
        info.inject(page.page, page.ctx.data());
        page.page.initialise(page.ctx);
        transitionViewersToInventory(inventory);
    }

    /**
     * Transition to another page. Adds the previous page to a stack which will be returned to when the current page is
     * closed.
     */
    public void transition(InventoryMenuPage instance) {
        transition(instance, Maps.newHashMap());
    }

    /**
     * Transition to another page with context. Adds the previous page to a stack which will be returned to when the
     * current page is closed.
     */
    public void transition(InventoryMenuPage instance, Map<String, Object> context) {
        Class<? extends InventoryMenuPage> clazz = instance.getClass();
        if (!CACHED_INFOS.containsKey(clazz)) {
            cacheInfo(clazz);
        }
        InventoryMenuInfo info = CACHED_INFOS.get(clazz);
        transition(info, instance, context);
    }

    private void transitionViewersToInventory(Inventory inventory) {
        Collection<InventoryView> old = views;
        views = Lists.newArrayListWithExpectedSize(old.size());
        for (InventoryView view : old) {
            view.close();
            if (!view.getPlayer().isValid() || inventory == null)
                continue;
            views.add(view.getPlayer().openInventory(inventory));
        }
    }

    private static class Bindable<T> {
        MethodHandle bind;
        T data;

        public Bindable(MethodHandle bind, T data) {
            this.bind = bind;
            this.data = data;
        }

        public void bind(Object instance, Object value) {
            if (bind == null)
                return;
            try {
                bind.invoke(instance, value);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static class InventoryMenuInfo {
        final Invokable<ClickHandler>[] clickHandlers;
        Constructor<? extends InventoryMenuPage> constructor;
        final Map<String, MethodHandle> injectables;
        Menu menuAnnotation;
        final Bindable<MenuPattern>[] patterns;
        final Bindable<MenuSlot>[] slots;
        final Bindable<MenuTransition>[] transitions;

        public InventoryMenuInfo(Class<?> clazz) {
            patterns = getBindables(clazz, MenuPattern.class, InventoryMenuPattern.class);
            slots = getBindables(clazz, MenuSlot.class, InventoryMenuSlot.class);
            transitions = getBindables(clazz, MenuTransition.class, InventoryMenuTransition.class);
            clickHandlers = getClickHandlers(clazz);
            injectables = getInjectables(clazz);
        }

        public InventoryMenuPage createInstance() {
            try {
                return constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings({ "unchecked" })
        private <T extends Annotation> Bindable<T>[] getBindables(Class<?> clazz, Class<T> annotationType,
                Class<?> concreteType) {
            List<Bindable<T>> bindables = Lists.newArrayList();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                T[] annotations = field.getAnnotationsByType(annotationType);
                MethodHandle bind = null;
                if (field.getType() == concreteType) {
                    try {
                        bind = LOOKUP.unreflectSetter(field);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                for (T t : annotations) {
                    bindables.add(new Bindable<T>(bind, t));
                }
            }

            List<AccessibleObject> reflect = Lists.newArrayList();
            reflect.addAll(Arrays.asList(clazz.getDeclaredConstructors()));
            reflect.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            for (AccessibleObject object : reflect) {
                object.setAccessible(true);
                for (T t : object.getAnnotationsByType(annotationType)) {
                    bindables.add(new Bindable<T>(null, t));
                }
            }
            for (T t : clazz.getAnnotationsByType(annotationType)) {
                bindables.add(new Bindable<T>(null, t));
            }
            return bindables.toArray(new Bindable[bindables.size()]);
        }

        @SuppressWarnings("unchecked")
        private Invokable<ClickHandler>[] getClickHandlers(Class<?> clazz) {
            List<Invokable<ClickHandler>> invokables = Lists.newArrayList();
            for (Method method : clazz.getDeclaredMethods()) {
                method.setAccessible(true);
                for (ClickHandler handler : method.getAnnotationsByType(ClickHandler.class)) {
                    try {
                        invokables.add(new Invokable<ClickHandler>(handler, LOOKUP.unreflect(method)));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            return invokables.toArray(new Invokable[invokables.size()]);
        }

        private Map<String, MethodHandle> getInjectables(Class<?> clazz) {
            Map<String, MethodHandle> injectables = Maps.newHashMap();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (!field.isAnnotationPresent(InjectContext.class))
                    continue;
                try {
                    injectables.put(field.getName(), LOOKUP.unreflectSetter(field));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return injectables;
        }

        public void inject(Object instance, Map<String, Object> data) {
            for (Map.Entry<String, MethodHandle> entry : injectables.entrySet()) {
                Object raw = data.get(entry.getKey());
                if (raw != null) {
                    try {
                        entry.getValue().invoke(instance, raw);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    }

    private static class Invokable<T> {
        private final T data;
        private final MethodHandle method;

        public Invokable(T data, MethodHandle invoke) {
            this.data = data;
            this.method = invoke;
        }
    }

    private static class PageContext {
        private Invokable<ClickHandler>[] clickHandlers;
        private MenuContext ctx;
        public int[] dim;
        private InventoryMenuPage page;
        private InventoryMenuPattern[] patterns;
        private InventoryMenuTransition[] transitions;
    }

    private static void cacheInfo(Class<? extends InventoryMenuPage> clazz) {
        InventoryMenuInfo info = new InventoryMenuInfo(clazz);
        info.menuAnnotation = clazz.getAnnotation(Menu.class);
        if (info.menuAnnotation == null) {
            throw new IllegalArgumentException("Missing menu annotation");
        }
        try {
            Constructor<? extends InventoryMenuPage> found = clazz.getDeclaredConstructor();
            found.setAccessible(true);
            info.constructor = found;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CACHED_INFOS.put(clazz, info);
    }

    /**
     * Create an inventory menu instance starting at the given page.
     */
    public static InventoryMenu create(Class<? extends InventoryMenuPage> clazz) {
        return createWithContext(clazz, Maps.newHashMap());
    }

    /**
     * Create an inventory menu instance starting at the given page.
     */
    public static InventoryMenu create(InventoryMenuPage instance) {
        Class<? extends InventoryMenuPage> clazz = instance.getClass();
        if (!CACHED_INFOS.containsKey(clazz)) {
            cacheInfo(clazz);
        }
        return new InventoryMenu(CACHED_INFOS.get(clazz), instance);
    }

    /**
     * Creates an inventory menu instance starting at the given page that registers events and deregisters events when
     * the menu is closed.
     */
    public static InventoryMenu createSelfRegistered(Class<? extends InventoryMenuPage> clazz) {
        InventoryMenu menu = create(clazz);
        Bukkit.getPluginManager().registerEvents(menu, CitizensAPI.getPlugin());
        menu.addCloseCallback(() -> HandlerList.unregisterAll(menu));
        return menu;
    }

    /**
     * Creates an inventory menu instance starting at the given page that registers events and deregisters events when
     * the menu is closed.
     */
    public static InventoryMenu createSelfRegistered(InventoryMenuPage instance) {
        InventoryMenu menu = create(instance);
        Bukkit.getPluginManager().registerEvents(menu, CitizensAPI.getPlugin());
        menu.addCloseCallback(() -> HandlerList.unregisterAll(menu));
        return menu;
    }

    /**
     * Create an inventory menu instance starting at the given page and with the initial context.
     */
    public static InventoryMenu createWithContext(Class<? extends InventoryMenuPage> clazz,
            Map<String, Object> context) {
        if (!CACHED_INFOS.containsKey(clazz)) {
            cacheInfo(clazz);
        }
        return new InventoryMenu(CACHED_INFOS.get(clazz), context);
    }

    private static Map<Class<? extends InventoryMenuPage>, InventoryMenuInfo> CACHED_INFOS = new WeakHashMap<Class<? extends InventoryMenuPage>, InventoryMenuInfo>();
}
