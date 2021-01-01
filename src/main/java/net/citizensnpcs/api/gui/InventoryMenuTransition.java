package net.citizensnpcs.api.gui;

public class InventoryMenuTransition {
    private final InventoryMenu menu;
    private final InventoryMenuSlot slot;
    private final Class<?> transition;

    public InventoryMenuTransition(InventoryMenu menu, InventoryMenuSlot slot, Class<?> transition) {
        this.menu = menu;
        this.slot = slot;
        this.transition = transition;
    }
}
