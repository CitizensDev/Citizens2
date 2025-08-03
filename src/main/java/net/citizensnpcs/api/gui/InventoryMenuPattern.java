package net.citizensnpcs.api.gui;

import java.util.List;

/**
 * The concrete instance of a {@link MenuPattern}. Defines a (possibly multiline) pattern with bound slots/transitions
 * depending on the pattern.
 */
public class InventoryMenuPattern {
    private final MenuPattern info;
    private final List<InventoryMenuSlot> slots;
    private final List<InventoryMenuTransition> transitions;

    public InventoryMenuPattern(MenuPattern info, List<InventoryMenuSlot> slots,
            List<InventoryMenuTransition> transitions) {
        this.info = info;
        this.slots = slots;
        this.transitions = transitions;
    }

    /**
     * @return The pattern string.
     */
    public String getPattern() {
        return info.value();
    }

    /**
     * @return The set of {@link InventoryMenuSlot}s that this pattern refers to.
     */
    public List<InventoryMenuSlot> getSlots() {
        return slots;
    }

    /**
     *
     * @return The set of {@link InventoryMenuTransition}s that this pattern refers to.
     */
    public List<InventoryMenuTransition> getTransitions() {
        return transitions;
    }
}
