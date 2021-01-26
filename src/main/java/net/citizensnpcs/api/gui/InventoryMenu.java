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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

// TODO: fix dim
public class InventoryMenu implements Listener {
    private PageContext page;
    private final Queue<PageContext> stack = Queues.newArrayDeque();
    private Collection<InventoryView> views = Lists.newArrayList();

    public InventoryMenu(InventoryMenuInfo info) {
        transition(info);
    }

    private boolean acceptFilter(ClickType needle, ClickType[] haystack) {
        for (ClickType type : haystack) {
            if (needle == type) {
                return true;
            }
        }
        return haystack.length == 0;
    }

    private InventoryMenuSlot createSlot(int pos, MenuSlot slotInfo) {
        InventoryMenuSlot slot = page.ctx.getSlot(pos);
        slot.initialise(slotInfo);
        return slot;
    }

    private InventoryMenuTransition createTransition(int pos, MenuTransition transitionInfo) {
        InventoryMenuSlot slot = page.ctx.getSlot(pos);
        InventoryMenuTransition transition = new InventoryMenuTransition(this, slot, transitionInfo.value());
        return transition;
    }

    private int getInventorySize(InventoryType type, int[] dim) {
        switch (type) {
            case CHEST:
                int size = dim[0] * dim[1];
                if (size % 9 != 0) {
                    size += 9 - (size % 9);
                }
                return Math.min(54, size);
            case ANVIL:
            case BLAST_FURNACE:
            case CARTOGRAPHY:
            case FURNACE:
            case GRINDSTONE:
            case SMITHING:
            case SMOKER:
                return 3;
            case BARREL:
            case ENDER_CHEST:
            case SHULKER_BOX:
                return 27;
            case BEACON:
            case LECTERN:
                return 1;
            case BREWING:
            case HOPPER:
                return 5;
            case DISPENSER:
            case DROPPER:
                return 9;
            case ENCHANTING:
            case STONECUTTER:
                return 2;
            case LOOM:
                return 4;
            case PLAYER:
                return 41;
            case WORKBENCH:
                return 10;
            default:
                throw new UnsupportedOperationException(); // TODO
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (page == null)
            return;
        Inventory clicked = event.getClickedInventory() != null ? event.getClickedInventory() : event.getInventory();
        if (event.getInventory().equals(page.ctx.getInventory())
                && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true); // TODO: treat this as a move-to-slot click event
        }
        if (page == null || !clicked.equals(page.ctx.getInventory()))
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
        page.page.onClick(slot, event);
        System.out.println(page.clickHandlers.length);
        for (Invokable<ClickHandler> invokable : page.clickHandlers) {
            int idx = posToIndex(page.dim, invokable.data.slot());
            if (event.getSlot() == idx && acceptFilter(event.getClick(), invokable.data.value())) {
                try {
                    // TODO: bind optional args?
                    invokable.method.invoke(page.page, slot, event);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        slot.onClick(event);
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
        page = stack.poll();
        transitionViewersToInventory(page == null ? null : page.ctx.getInventory());
    }

    private InventoryMenuPattern parsePattern(int[] dim, List<InventoryMenuTransition> transitions,
            Bindable<MenuPatternInfo> patternInfo) {
        String pattern = patternInfo.data.info.value();
        Map<Character, MenuSlot> slotMap = Maps.newHashMap();
        for (MenuSlot slot : patternInfo.data.slots) {
            slotMap.put(slot.pat(), slot);
        }
        Map<Character, MenuTransition> transitionMap = Maps.newHashMap();
        for (MenuTransition transition : patternInfo.data.transitions) {
            transitionMap.put(transition.pat(), transition);
        }

        Collection<InventoryMenuSlot> patternSlots = Lists.newArrayList();
        Collection<InventoryMenuTransition> patternTransitions = Lists.newArrayList();
        int row = 0;
        int col = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '\n' || (c == '\\' && i + 1 < pattern.length() && pattern.charAt(i + 1) == 'n')) {
                i++;
                row++;
                col = 0;
                continue;
            }
            int[] pos = patternInfo.data.info.offset();
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

        return new InventoryMenuPattern(patternInfo.data.info, patternSlots, patternTransitions);
    }

    private int posToIndex(int[] dim, int[] pos) {
        return pos[0] * dim[1] + pos[1];
    }

    public void present(Player player) {
        InventoryView view = player.openInventory(page.ctx.getInventory());
        views.add(view);
    }

    public void transition(Class<? extends InventoryMenuPage> clazz) {
        if (!CACHED_INFOS.containsKey(clazz)) {
            cacheInfo(clazz);
        }
        transition(CACHED_INFOS.get(clazz));
    }

    private void transition(InventoryMenuInfo info) {
        if (page != null) {
            stack.add(page);
        }
        page = new PageContext();
        int[] dim = info.menuAnnotation.dimensions();
        InventoryType type = info.menuAnnotation.type();
        int size = getInventorySize(type, dim);
        Inventory inventory;
        if (type == InventoryType.CHEST || type == null) {
            inventory = Bukkit.createInventory(null, size, info.menuAnnotation.title());
        } else {
            inventory = Bukkit.createInventory(null, type, info.menuAnnotation.title());
        }
        List<InventoryMenuTransition> transitions = Lists.newArrayList();
        InventoryMenuSlot[] slots = new InventoryMenuSlot[inventory.getSize()];
        page.patterns = new InventoryMenuPattern[info.patterns.length];
        try {
            page.page = info.constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        page.dim = dim;
        page.ctx = new MenuContext(this, slots, inventory);
        for (int i = 0; i < info.slots.length; i++) {
            Bindable<MenuSlot> slotInfo = info.slots[i];
            int pos = posToIndex(dim, slotInfo.data.value());
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
            Bindable<MenuPatternInfo> patternInfo = info.patterns[i];
            InventoryMenuPattern pattern = parsePattern(dim, transitions, patternInfo);
            patternInfo.bind(page.page, pattern);
            page.patterns[i] = pattern;
        }
        page.transitions = transitions.toArray(new InventoryMenuTransition[transitions.size()]);
        page.clickHandlers = info.clickHandlers;
        transitionViewersToInventory(inventory);
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
        Invokable<ClickHandler>[] clickHandlers;
        Constructor<? extends InventoryMenuPage> constructor;
        Menu menuAnnotation;
        Bindable<MenuPatternInfo>[] patterns;
        Bindable<MenuSlot>[] slots;
        Bindable<MenuTransition>[] transitions;

        public InventoryMenuInfo(Class<?> clazz) {
            patterns = getPatternBindables(clazz);
            slots = getBindables(clazz, MenuSlot.class, InventoryMenuSlot.class);
            transitions = getBindables(clazz, MenuTransition.class, InventoryMenuTransition.class);
            clickHandlers = getHandlers(clazz);
        }

        @SuppressWarnings({ "unchecked" })
        private <T extends Annotation> Bindable<T>[] getBindables(Class<?> clazz, Class<T> annotationType,
                Class<?> concreteType) {
            List<Bindable<T>> bindables = Lists.newArrayList();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getAnnotationsByType(MenuPattern.class).length != 0)
                    continue;
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
                if (object.getAnnotationsByType(MenuPattern.class).length != 0)
                    continue;
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
        private Invokable<ClickHandler>[] getHandlers(Class<?> clazz) {
            List<Invokable<ClickHandler>> invokables = Lists.newArrayList();
            for (Method method : clazz.getDeclaredMethods()) {
                method.setAccessible(true);
                ClickHandler handler = method.getAnnotation(ClickHandler.class);
                if (handler == null) {
                    continue;
                }
                try {
                    invokables.add(new Invokable<ClickHandler>(handler, LOOKUP.unreflect(method)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return invokables.toArray(new Invokable[invokables.size()]);
        }

        private Bindable<MenuPatternInfo> getPatternBindable(MethodHandle bind, AccessibleObject object) {
            MenuPattern[] annotation = object.getAnnotationsByType(MenuPattern.class);
            if (annotation.length != 1)
                return null;
            MenuPattern pattern = annotation[0];
            Collection<MenuSlot> slots = Lists.newArrayList();
            for (MenuSlot slot : object.getAnnotationsByType(MenuSlot.class)) {
                if (slot.pat() != '0' && pattern.value().contains(Character.toString(slot.pat()))) {
                    slots.add(slot);
                }
            }
            Collection<MenuTransition> transitions = Lists.newArrayList();
            for (MenuTransition transition : object.getAnnotationsByType(MenuTransition.class)) {
                if (transition.pat() != '0' && pattern.value().contains(Character.toString(transition.pat()))) {
                    transitions.add(transition);
                }
            }
            return new Bindable<MenuPatternInfo>(bind, new MenuPatternInfo(pattern, slots, transitions));
        }

        private Bindable<MenuPatternInfo> getPatternBindable(MethodHandle bind, Class<?> object) {
            MenuPattern[] annotation = object.getAnnotationsByType(MenuPattern.class);
            if (annotation.length != 1)
                return null;
            MenuPattern pattern = annotation[0];
            Collection<MenuSlot> slots = Lists.newArrayList();
            for (MenuSlot slot : object.getAnnotationsByType(MenuSlot.class)) {
                if (slot.pat() != '0' && pattern.value().contains(Character.toString(slot.pat()))) {
                    slots.add(slot);
                }
            }
            Collection<MenuTransition> transitions = Lists.newArrayList();
            for (MenuTransition transition : object.getAnnotationsByType(MenuTransition.class)) {
                if (transition.pat() != '0' && pattern.value().contains(Character.toString(transition.pat()))) {
                    transitions.add(transition);
                }
            }
            return new Bindable<MenuPatternInfo>(bind, new MenuPatternInfo(pattern, slots, transitions));
        }

        @SuppressWarnings({ "unchecked" })
        private Bindable<MenuPatternInfo>[] getPatternBindables(Class<?> clazz) {
            Collection<Bindable<MenuPatternInfo>> bindables = Lists.newArrayList();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                MethodHandle bind = null;
                if (field.getType() == InventoryMenuPattern.class) {
                    try {
                        bind = LOOKUP.unreflectSetter(field);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                bindables.add(getPatternBindable(bind, field));
            }

            List<AccessibleObject> reflect = Lists.newArrayList();
            reflect.addAll(Arrays.asList(clazz.getDeclaredConstructors()));
            reflect.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            for (AccessibleObject object : reflect) {
                object.setAccessible(true);
                if (object.getAnnotationsByType(MenuPattern.class).length != 0)
                    continue;
                bindables.add(getPatternBindable(null, object));
            }
            bindables.add(getPatternBindable(null, clazz));
            return Collections2.filter(bindables, Predicates.notNull()).toArray(new Bindable[0]);
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

    private static class MenuPatternInfo {
        MenuPattern info;
        Collection<MenuSlot> slots = Lists.newArrayList();
        Collection<MenuTransition> transitions = Lists.newArrayList();

        public MenuPatternInfo(MenuPattern info, Collection<MenuSlot> slots, Collection<MenuTransition> transitions) {
            this.info = info;
            this.slots = slots;
            this.transitions = transitions;
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
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        CACHED_INFOS.put(clazz, info);
    }

    public static InventoryMenu create(Class<? extends InventoryMenuPage> clazz) {
        if (!CACHED_INFOS.containsKey(clazz)) {
            cacheInfo(clazz);
        }
        InventoryMenuInfo info = CACHED_INFOS.get(clazz);
        return new InventoryMenu(info);
    }

    private static Map<Class<? extends InventoryMenuPage>, InventoryMenuInfo> CACHED_INFOS = new WeakHashMap<Class<? extends InventoryMenuPage>, InventoryMenuInfo>();
}
