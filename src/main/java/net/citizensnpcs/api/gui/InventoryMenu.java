package net.citizensnpcs.api.gui;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class InventoryMenu {
    private final MenuContext currentContext;
    private InventoryMenuPage page;
    private final InventoryMenuPattern[] patterns;
    private final InventoryMenuTransition[] transitions;

    public InventoryMenu(InventoryMenuInfo info) {
        int[] dim = info.menuAnnotation.dimensions();
        int size = dim[0] * dim[1];
        Inventory inventory;
        if (info.menuAnnotation.type() == InventoryType.CHEST || info.menuAnnotation.type() == null) {
            inventory = Bukkit.createInventory(null, size, info.menuAnnotation.title());
        } else {
            inventory = Bukkit.createInventory(null, info.menuAnnotation.type(), info.menuAnnotation.title());
        }
        InventoryMenuSlot[] slots = new InventoryMenuSlot[inventory.getSize()];
        this.patterns = new InventoryMenuPattern[info.patterns.length];
        this.transitions = new InventoryMenuTransition[info.transitions.length];
        try {
            this.page = info.constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        currentContext = new MenuContext(this, slots, inventory);
        for (int i = 0; i < info.slots.length; i++) {
            Bindable<MenuSlot> slotInfo = info.slots[i];
            int pos = posToIndex(dim, slotInfo.data.value());
            InventoryMenuSlot slot = currentContext.getSlot(pos);
            slot.initialise(slotInfo.data);
            slotInfo.bind(slot);
        }
        for (int i = 0; i < info.patterns.length; i++) {
            Bindable<MenuPattern> patternInfo = info.patterns[i];
            InventoryMenuPattern pat = new InventoryMenuPattern(currentContext, patternInfo.data);
            patternInfo.bind(pat);
            this.patterns[i] = pat;
        }
        for (int i = 0; i < info.transitions.length; i++) {
            Bindable<MenuTransition> transitionInfo = info.transitions[i];
            int pos = posToIndex(dim, transitionInfo.data.pos());
            InventoryMenuSlot slot = currentContext.getSlot(pos);
            InventoryMenuTransition transition = new InventoryMenuTransition(this, slot, transitionInfo.data.value());
            transitionInfo.bind(transition);
            this.transitions[i] = transition;
        }
    }

    private int posToIndex(int[] dim, int[] pos) {
        return pos[0] * dim[1] + pos[1];
    }

    private static class Bindable<T> {
        MethodHandle bind;
        T data;

        public void bind(Object slot) {
            if (bind == null)
                return;
            try {
                bind.invoke(slot);
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
    }

    public static InventoryMenu create(Class<? extends InventoryMenuPage> clazz) {
        if (!CACHED_INFOS.containsKey(clazz)) {
            create0(clazz);
        }
        InventoryMenuInfo info = CACHED_INFOS.get(clazz);
        return new InventoryMenu(info);
    }

    private static void create0(Class<? extends InventoryMenuPage> clazz) {
        InventoryMenuInfo info = new InventoryMenuInfo();
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

    private static Map<Class<? extends InventoryMenuPage>, InventoryMenuInfo> CACHED_INFOS = new WeakHashMap<Class<? extends InventoryMenuPage>, InventoryMenuInfo>();
}
