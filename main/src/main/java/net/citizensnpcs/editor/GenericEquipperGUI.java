package net.citizensnpcs.editor;

import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.gui.CitizensInventoryClickEvent;
import net.citizensnpcs.api.gui.InjectContext;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.gui.MenuPattern;
import net.citizensnpcs.api.gui.MenuSlot;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.util.Util;

@Menu(title = "NPC Equipment", type = InventoryType.CHEST, dimensions = { 3, 9 })
@MenuSlot(slot = { 0, 0 }, material = Material.DIAMOND_SWORD, lore = "Place in hand item below", amount = 1)
@MenuSlot(
        slot = { 0, 1 },
        compatMaterial = { "SHIELD", "BARRIER", "FIRE" },
        lore = "Place offhand item below",
        amount = 1)
@MenuSlot(slot = { 0, 2 }, material = Material.DIAMOND_HELMET, lore = "Place helmet below", amount = 1)
@MenuSlot(slot = { 0, 3 }, material = Material.DIAMOND_CHESTPLATE, lore = "Place chestplate below", amount = 1)
@MenuSlot(slot = { 0, 4 }, material = Material.DIAMOND_LEGGINGS, lore = "Place leggings below", amount = 1)
@MenuSlot(slot = { 0, 5 }, material = Material.DIAMOND_BOOTS, lore = "Place boots below", amount = 1)
@MenuSlot(slot = { 0, 6 }, material = Material.DIAMOND_CHESTPLATE, lore = "Place body item below", amount = 1)
@MenuPattern(
        offset = { 0, 7 },
        slots = { @MenuSlot(pat = 'x', compatMaterial = { "BARRIER", "FIRE" }, title = "<4>Unused") },
        value = "xx\nxx\nxx")
public class GenericEquipperGUI extends InventoryMenuPage {
    @InjectContext
    private NPC npc;

    @Override
    public void initialise(MenuContext ctx) {
        Equipment trait = npc.getOrAddTrait(Equipment.class);
        EquipmentSlot[] slots = new EquipmentSlot[] { EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.HELMET,
                EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS, EquipmentSlot.BODY };
        for (int i = 0; i < slots.length; i++) {
            EquipmentSlot slot = slots[i];
            ctx.getSlot(1 * 9 + i).setItemStack(trait.get(slot));
            if (trait.getCosmetic(slot) != null) {
                ctx.getSlot(2 * 9 + i).setItemStack(trait.getCosmetic(slot));
            } else {
                ctx.getSlot(2 * 9 + i).setItemStack(Util.createItem(Util.getFallbackMaterial("BARRIER", "FIRE"),
                        "No cosmetic", "Click to enable cosmetic for this equipment"));
            }
            Function<ItemStack, Boolean> filter = type -> true;
            switch (slot) {
                case BOOTS:
                case LEGGINGS:
                    filter = stack -> Util.isEquippable(stack, slot) || stack.getType().name().endsWith(slot.name());
                    break;
                case CHESTPLATE:
                    filter = stack -> Util.isEquippable(stack, slot) || stack.getType() == Material.ELYTRA
                            || stack.getType().name().endsWith(slot.name());
                default:
                    break;
            }
            Function<ItemStack, Boolean> ffilter = filter;
            ctx.getSlot(1 * 9 + i).addClickHandler(event -> set(slot, event, ffilter));
            ctx.getSlot(2 * 9 + i).addClickHandler(event -> setCosmetic(slot, event, ffilter));
        }
    }

    private void set(EquipmentSlot slot, CitizensInventoryClickEvent event, Function<ItemStack, Boolean> filter) {
        ItemStack result = event.getResultItemNonNull();
        if (event.isCancelled() || (result.getType() != Material.AIR && !filter.apply(result))) {
            event.setResult(Result.DENY);
            return;
        }
        npc.getOrAddTrait(Equipment.class).set(slot, result);
    }

    private void setCosmetic(EquipmentSlot slot, CitizensInventoryClickEvent event,
            Function<ItemStack, Boolean> filter) {
        if (event.getCursorNonNull().getType() == Material.AIR) {
            if (event.getCurrentItemNonNull().getType() == Util.getFallbackMaterial("BARRIER", "FIRE")) {
                event.setCurrentItem(null);
                npc.getOrAddTrait(Equipment.class).setCosmetic(slot, new ItemStack(Material.AIR, 1));
            } else if (event.getCurrentItem() == null) {
                event.setCurrentItem(Util.createItem(Util.getFallbackMaterial("BARRIER", "FIRE"), "No cosmetic",
                        "Click to enable cosmetic for this equipment"));
                npc.getOrAddTrait(Equipment.class).setCosmetic(slot, null);
            }
            event.setResult(Result.DENY);
            return;
        }
        ItemStack result = event.getResultItemNonNull();
        if (event.isCancelled() || (result.getType() != Material.AIR && !filter.apply(result))) {
            event.setResult(Result.DENY);
            return;
        }
        npc.getOrAddTrait(Equipment.class).setCosmetic(slot, result);
    }
}
