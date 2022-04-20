package net.citizensnpcs.api.gui;

import org.bukkit.event.inventory.InventoryType;

@Menu(type = InventoryType.ANVIL)
public class InputMenu extends InventoryMenuPage {
    private MenuContext ctx;

    @ClickHandler(slot = { 0, 0 })
    public void cancel(InventoryMenuSlot slot, CitizensInventoryClickEvent evt) {
    }

    @Override
    public void initialise(MenuContext ctx) {
        this.ctx = ctx;
    }

    @ClickHandler(slot = { 0, 1 })
    public void save(InventoryMenuSlot slot, CitizensInventoryClickEvent evt) {
    }
}
