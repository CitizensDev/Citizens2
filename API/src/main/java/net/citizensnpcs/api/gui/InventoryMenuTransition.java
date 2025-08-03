package net.citizensnpcs.api.gui;

/**
 * The concrete class of {@link MenuTransition}. Defines a transition from one {@link InventoryMenuPage} to another when
 * clicked.
 */
public class InventoryMenuTransition {
    private final InventoryMenuSlot slot;
    private final Class<? extends InventoryMenuPage> transition;

    public InventoryMenuTransition(InventoryMenuSlot slot, Class<? extends InventoryMenuPage> transition) {
        this.slot = slot;
        this.transition = transition;
    }

    Class<? extends InventoryMenuPage> accept(InventoryMenuSlot accept) {
        return accept.equals(slot) ? transition : null;
    }

    /**
     * @return The slot holding the transition
     */
    public InventoryMenuSlot getSlot() {
        return slot;
    }
}
