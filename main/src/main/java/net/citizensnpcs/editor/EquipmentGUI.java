package net.citizensnpcs.editor;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.InventoryMenuSlot;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.gui.MenuSlot;
import net.citizensnpcs.api.util.Messaging;

@Menu(title = "NPC Equipment", type = InventoryType.CHEST, dimensions = { 3, 3 })
public class EquipmentGUI extends InventoryMenuPage {
    @MenuSlot(value = { 0, 1 }, material = Material.DIAMOND_SWORD, amount = 1)
    private InventoryMenuSlot slot;

    @Override
    public void create(MenuContext ctx) {
    }

    @Override
    public void onClick(InventoryMenuSlot slot, InventoryClickEvent event) {
        Messaging.log(event);
    }

    @Override
    public void onClose(HumanEntity player) {
        Messaging.log("CLOSED", player);
    }
}
