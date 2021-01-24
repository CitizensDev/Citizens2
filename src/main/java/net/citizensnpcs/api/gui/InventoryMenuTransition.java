package net.citizensnpcs.api.gui;

public class InventoryMenuTransition {
    private final InventoryMenu menu;
    private final InventoryMenuSlot slot;
    private final Class<? extends InventoryMenuPage> transition;

    public InventoryMenuTransition(InventoryMenu menu, InventoryMenuSlot slot,
            Class<? extends InventoryMenuPage> transition) {
        this.menu = menu;
        this.slot = slot;
        this.transition = transition;
    }

    public Class<? extends InventoryMenuPage> accept(InventoryMenuSlot accept) {
        return accept.equals(slot) ? transition : null;
    }
}
