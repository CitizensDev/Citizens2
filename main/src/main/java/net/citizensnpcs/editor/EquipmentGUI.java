package net.citizensnpcs.editor;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

import net.citizensnpcs.api.gui.ClickHandler;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.InventoryMenuPattern;
import net.citizensnpcs.api.gui.InventoryMenuSlot;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.gui.MenuPattern;
import net.citizensnpcs.api.gui.MenuSlot;
import net.citizensnpcs.api.util.Messaging;

@Menu(title = "NPC Equipment", type = InventoryType.CHEST, dimensions = { 2, 5 })
@MenuSlot(value = { 0, 0 }, material = Material.DIAMOND_SWORD, amount = 1, filter = ClickType.UNKNOWN)
@MenuSlot(value = { 0, 1 }, material = Material.ELYTRA, amount = 1, filter = ClickType.UNKNOWN)
@MenuSlot(value = { 0, 2 }, material = Material.DIAMOND_HELMET, amount = 1, filter = ClickType.UNKNOWN)
@MenuSlot(value = { 0, 3 }, material = Material.DIAMOND_CHESTPLATE, amount = 1, filter = ClickType.UNKNOWN)
@MenuSlot(value = { 0, 4 }, material = Material.DIAMOND_LEGGINGS, amount = 1, filter = ClickType.UNKNOWN)
@MenuSlot(value = { 0, 5 }, material = Material.DIAMOND_BOOTS, amount = 1, filter = ClickType.UNKNOWN)
public class EquipmentGUI extends InventoryMenuPage {
    @MenuPattern(offset = { 0, 5 }, value = "xxx\nxxx")
    @MenuSlot(filter = ClickType.UNKNOWN, pat = 'x')
    private InventoryMenuPattern block;
    @MenuSlot({ 1, 5 })
    private InventoryMenuSlot boots;
    @MenuSlot({ 1, 3 })
    private InventoryMenuSlot chest;
    @MenuSlot({ 1, 0 })
    private InventoryMenuSlot hand;
    @MenuSlot({ 1, 2 })
    private InventoryMenuSlot helmet;
    @MenuSlot({ 1, 4 })
    private InventoryMenuSlot leggings;
    @MenuSlot({ 1, 1 })
    private InventoryMenuSlot offhand;

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

    @ClickHandler(slot = { 1, 5 })
    public void setBoots(InventoryMenuSlot slot, InventoryClickEvent event) {
        Messaging.log("BOOTS", event);
    }

    @ClickHandler(slot = { 1, 3 })
    public void setChest(InventoryMenuSlot slot, InventoryClickEvent event) {
    }

    @ClickHandler(slot = { 1, 0 })
    public void setHand(InventoryMenuSlot slot, InventoryClickEvent event) {
        Messaging.log("HAND", event);
    }

    @ClickHandler(slot = { 1, 2 })
    public void setHelmet(InventoryMenuSlot slot, InventoryClickEvent event) {
    }

    @ClickHandler(slot = { 1, 4 })
    public void setLeggings(InventoryMenuSlot slot, InventoryClickEvent event) {
    }

    @ClickHandler(slot = { 1, 1 })
    public void setOffhand(InventoryMenuSlot slot, InventoryClickEvent event) {
    }
}
