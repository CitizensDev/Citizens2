package net.citizensnpcs.api.gui;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

public class InventoryMenu implements Listener {
    private PageContext page;
    private final Queue<PageContext> stack = Queues.newArrayDeque();
    private Collection<InventoryView> views = Lists.newArrayList();

    public InventoryMenu(InventoryMenuInfo info) {
        transition(info);
    }

    private InventoryMenuSlot createSlot(int[] dim, MenuSlot slotInfo) {
        int pos = posToIndex(dim, slotInfo.value());
        InventoryMenuSlot slot = page.ctx.getSlot(pos);
        slot.initialise(slotInfo);
        return slot;
    }

    private InventoryMenuTransition createTransition(int[] dim, MenuTransition transitionInfo) {
        int pos = posToIndex(dim, transitionInfo.pos());
        InventoryMenuSlot slot = page.ctx.getSlot(pos);
        InventoryMenuTransition transition = new InventoryMenuTransition(this, slot, transitionInfo.value());
        return transition;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(page.ctx.getInventory()))
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
        if (!event.getInventory().equals(page.ctx.getInventory()))
            return;
        page.page.onClose(event.getPlayer());
        page = stack.poll();
        transitionViewersToInventory(page.ctx.getInventory());
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
        int size = dim[0] * dim[1];
        Inventory inventory;
        if (info.menuAnnotation.type() == InventoryType.CHEST || info.menuAnnotation.type() == null) {
            inventory = Bukkit.createInventory(null, size, info.menuAnnotation.title());
        } else {
            inventory = Bukkit.createInventory(null, info.menuAnnotation.type(), info.menuAnnotation.title());
        }
        List<InventoryMenuTransition> transitions = Lists.newArrayList();
        InventoryMenuSlot[] slots = new InventoryMenuSlot[inventory.getSize()];
        page.patterns = new InventoryMenuPattern[info.patterns.length];
        try {
            page.page = info.constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        page.ctx = new MenuContext(this, slots, inventory);
        for (int i = 0; i < info.slots.length; i++) {
            Bindable<MenuSlot> slotInfo = info.slots[i];
            InventoryMenuSlot slot = createSlot(dim, slotInfo.data);
            slotInfo.bind(page.page, slot);
        }
        for (int i = 0; i < info.transitions.length; i++) {
            Bindable<MenuTransition> transitionInfo = info.transitions[i];
            InventoryMenuTransition transition = createTransition(dim, transitionInfo.data);
            transitionInfo.bind(page.page, transition);
            transitions.add(transition);
        }
        for (int i = 0; i < info.patterns.length; i++) {
            Bindable<MenuPatternInfo> patternInfo = info.patterns[i];
            Collection<InventoryMenuSlot> patternSlots = Lists.newArrayList();
            Collection<InventoryMenuTransition> patternTransitions = Lists.newArrayList();
            for (MenuSlot slot : patternInfo.data.slots) {
                patternSlots.add(createSlot(dim, slot));
            }
            for (MenuTransition transition : patternInfo.data.transitions) {
                InventoryMenuTransition concreteTransition = createTransition(dim, transition);
                patternTransitions.add(concreteTransition);
                transitions.add(concreteTransition);
            }
            InventoryMenuPattern pat = new InventoryMenuPattern(patternInfo.data.info, patternSlots,
                    patternTransitions);
            patternInfo.bind(page.page, pat);
            page.patterns[i] = pat;
        }
        page.transitions = transitions.toArray(new InventoryMenuTransition[transitions.size()]);
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
        Constructor<? extends InventoryMenuPage> constructor;
        Menu menuAnnotation;
        Bindable<MenuPatternInfo>[] patterns;
        Bindable<MenuSlot>[] slots;
        Bindable<MenuTransition>[] transitions;

        public InventoryMenuInfo(Class<?> clazz) {
            patterns = getPatternBindables(clazz);
            slots = getBindables(clazz, MenuSlot.class, InventoryMenuSlot.class);
            transitions = getBindables(clazz, MenuTransition.class, InventoryMenuTransition.class);
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

        private Bindable<MenuPatternInfo> getPatternBindable(MethodHandle bind, AccessibleObject object) {
            MenuPattern[] annotation = object.getAnnotationsByType(MenuPattern.class);
            MenuPattern pattern = annotation[0];
            Collection<MenuSlot> slots = Lists.newArrayList();
            for (MenuSlot slot : object.getAnnotationsByType(MenuSlot.class)) {
                if (pattern.value().contains(Character.toString(slot.pat()))) {
                    slots.add(slot);
                }
            }
            Collection<MenuTransition> transitions = Lists.newArrayList();
            for (MenuTransition transition : object.getAnnotationsByType(MenuTransition.class)) {
                if (pattern.value().contains(Character.toString(transition.pat()))) {
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
                if (pattern.value().contains(Character.toString(slot.pat()))) {
                    slots.add(slot);
                }
            }
            Collection<MenuTransition> transitions = Lists.newArrayList();
            for (MenuTransition transition : object.getAnnotationsByType(MenuTransition.class)) {
                if (pattern.value().contains(Character.toString(transition.pat()))) {
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
        private MenuContext ctx;
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
