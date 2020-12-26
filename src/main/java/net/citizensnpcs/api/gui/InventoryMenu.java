package net.citizensnpcs.api.gui;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class InventoryMenu {
    private final Inventory inventory;
    private InventoryMenuPage page;
    private final InventoryMenuPattern[] patterns;
    private final InventoryMenuSlot[] slots;
    private final InventoryMenuTransition[] transitions;

    public InventoryMenu(InventoryMenuInfo info) {
        int[] dim = info.menuAnnotation.dimensions();
        int size = dim[0] * dim[1];
        if (info.menuAnnotation.type() == InventoryType.CHEST || info.menuAnnotation.type() == null) {
            this.inventory = Bukkit.createInventory(null, size, info.menuAnnotation.title());
        } else {
            this.inventory = Bukkit.createInventory(null, info.menuAnnotation.type(), info.menuAnnotation.title());
        }
        this.slots = new InventoryMenuSlot[this.inventory.getSize()];
        this.patterns = new InventoryMenuPattern[info.patterns.length];
        this.transitions = new InventoryMenuTransition[info.transitions.length];
        try {
            this.page = info.constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < info.slots.length; i++) {
            Bindable<MenuSlot> slotInfo = info.slots[i];
            int pos = slotInfo.data.value()[0] * dim[0] + slotInfo.data.value()[1];
            slotInfo.data.value();
            InventoryMenuSlot slot = getSlot(pos);
        }
        for (int i = 0; i < info.patterns.length; i++) {
            Bindable<MenuPattern> patternInfo = info.patterns[i];
            InventoryMenuPattern c = new InventoryMenuPattern();
        }
        for (int i = 0; i < info.transitions.length; i++) {
            Bindable<MenuTransition> transitionInfo = info.transitions[i];
        }
    }

    private InventoryMenuSlot getSlot(int i) {
        return slots[i] == null ? slots[i] = new InventoryMenuSlot(this, i) : slots[i];
    }

    private static class Bindable<T> {
        MethodHandle bind;
        T data;
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
