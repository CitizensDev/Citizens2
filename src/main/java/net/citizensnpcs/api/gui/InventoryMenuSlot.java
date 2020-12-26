package net.citizensnpcs.api.gui;

public class InventoryMenuSlot {
    private final int index;
    private final InventoryMenu menu;

    public InventoryMenuSlot(InventoryMenu menu, int i) {
        this.menu = menu;
        this.index = i;
    }
}
