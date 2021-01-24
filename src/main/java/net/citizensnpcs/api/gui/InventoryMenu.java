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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

public class InventoryMenu implements Listener {
    private PageContext page;
    private final Queue<PageContext> stack = Queues.newArrayDeque();
    private final Collection<InventoryView> views = Lists.newArrayList();

    public InventoryMenu(InventoryMenuInfo info) {
        transition(info);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(page.ctx.getInventory()) || event.getAction() == InventoryAction.NOTHING)
            return;
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
        InventoryMenuSlot[] slots = new InventoryMenuSlot[inventory.getSize()];
        page.patterns = new InventoryMenuPattern[info.patterns.length];
        page.transitions = new InventoryMenuTransition[info.transitions.length];
        try {
            page.page = info.constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        page.ctx = new MenuContext(this, slots, inventory);
        for (int i = 0; i < info.slots.length; i++) {
            Bindable<MenuSlot> slotInfo = info.slots[i];
            int pos = posToIndex(dim, slotInfo.data.value());
            InventoryMenuSlot slot = page.ctx.getSlot(pos);
            slot.initialise(slotInfo.data);
            slotInfo.bind(slot);
        }
        for (int i = 0; i < info.patterns.length; i++) {
            Bindable<MenuPattern> patternInfo = info.patterns[i];
            InventoryMenuPattern pat = new InventoryMenuPattern(page.ctx, patternInfo.data);
            patternInfo.bind(pat);
            page.patterns[i] = pat;
        }
        for (int i = 0; i < info.transitions.length; i++) {
            Bindable<MenuTransition> transitionInfo = info.transitions[i];
            int pos = posToIndex(dim, transitionInfo.data.pos());
            InventoryMenuSlot slot = page.ctx.getSlot(pos);
            InventoryMenuTransition transition = new InventoryMenuTransition(this, slot, transitionInfo.data.value());
            transitionInfo.bind(transition);
            page.transitions[i] = transition;
        }
    }

    private static class Bindable<T> {
        MethodHandle bind;
        T data;

        public Bindable(MethodHandle bind, T data) {
            this.bind = bind;
            this.data = data;
        }

        public void bind(Object instance) {
            if (bind == null)
                return;
            try {
                bind.invoke(instance);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static class InventoryMenuInfo {
        Constructor<? extends InventoryMenuPage> constructor;
        Menu menuAnnotation;
        Bindable<MenuPattern>[] patterns;
        Bindable<MenuSlot>[] slots;
        Bindable<MenuTransition>[] transitions;

        public InventoryMenuInfo(Class<?> clazz) {
            patterns = getBindables(clazz, MenuPattern.class, InventoryMenuPattern.class);
            slots = getBindables(clazz, MenuSlot.class, InventoryMenuSlot.class);
            transitions = getBindables(clazz, MenuTransition.class, InventoryMenuTransition.class);
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

        private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
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
