package net.citizensnpcs.editor;

import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.gui.CitizensInventoryClickEvent;
import net.citizensnpcs.api.gui.ClickHandler;
import net.citizensnpcs.api.gui.InjectContext;
import net.citizensnpcs.api.gui.InventoryMenuPage;
import net.citizensnpcs.api.gui.InventoryMenuSlot;
import net.citizensnpcs.api.gui.Menu;
import net.citizensnpcs.api.gui.MenuContext;
import net.citizensnpcs.api.gui.MenuPattern;
import net.citizensnpcs.api.gui.MenuSlot;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;

@Menu(title = "NPC Equipment", type = InventoryType.CHEST, dimensions = { 2, 5 })
@MenuSlot(
        slot = { 0, 1 },
        compatMaterial = { "SHIELD", "BARRIER", "FIRE" },
        lore = "Place offhand item below",
        amount = 1)
@MenuSlot(slot = { 0, 0 }, material = Material.DIAMOND_SWORD, lore = "Place in hand item below", amount = 1)
@MenuSlot(slot = { 0, 2 }, material = Material.DIAMOND_HELMET, lore = "Place helmet below", amount = 1)
@MenuSlot(slot = { 0, 3 }, material = Material.DIAMOND_CHESTPLATE, lore = "Place chestplate below", amount = 1)
@MenuSlot(slot = { 0, 4 }, material = Material.DIAMOND_LEGGINGS, lore = "Place leggings below", amount = 1)
@MenuSlot(slot = { 0, 5 }, material = Material.DIAMOND_BOOTS, lore = "Place boots below", amount = 1)
@MenuPattern(
        offset = { 0, 6 },
        slots = { @MenuSlot(pat = 'x', compatMaterial = { "BARRIER", "FIRE" }, title = "<4>Unused") },
        value = "xxx\nxxx")
public class GenericEquipperGUI extends InventoryMenuPage {
    @MenuSlot(slot = { 1, 5 })
    private InventoryMenuSlot boots;
    @MenuSlot(slot = { 1, 3 })
    private InventoryMenuSlot chest;
    @MenuSlot(slot = { 1, 0 })
    private InventoryMenuSlot hand;
    @MenuSlot(slot = { 1, 2 })
    private InventoryMenuSlot helmet;
    @MenuSlot(slot = { 1, 4 })
    private InventoryMenuSlot leggings;
    @InjectContext
    private NPC npc;
    @MenuSlot(slot = { 1, 1 })
    private InventoryMenuSlot offhand;

    @Override
    public void initialise(MenuContext ctx) {
        Equipment trait = npc.getOrAddTrait(Equipment.class);
        hand.setItemStack(trait.get(EquipmentSlot.HAND));
        helmet.setItemStack(trait.get(EquipmentSlot.HELMET));
        chest.setItemStack(trait.get(EquipmentSlot.CHESTPLATE));
        leggings.setItemStack(trait.get(EquipmentSlot.LEGGINGS));
        boots.setItemStack(trait.get(EquipmentSlot.BOOTS));
        offhand.setItemStack(trait.get(EquipmentSlot.OFF_HAND));
    }

    private void set(EquipmentSlot slot, CitizensInventoryClickEvent event, Function<Material, Boolean> filter) {
        ItemStack result = event.getResultItemNonNull();
        if (event.isCancelled() || !filter.apply(result.getType())) {
            event.setResult(Result.DENY);
            return;
        }
        npc.getOrAddTrait(Equipment.class).set(slot, result);
    }

    @ClickHandler(slot = { 1, 5 })
    public void setBoots(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
        set(EquipmentSlot.BOOTS, event, type -> type == Material.AIR || type.name().endsWith("BOOTS"));
    }

    @ClickHandler(slot = { 1, 3 })
    public void setChest(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
        set(EquipmentSlot.CHESTPLATE, event,
                type -> type == Material.AIR || type.name().endsWith("CHESTPLATE") || type.name().equals("ELYTRA"));
    }

    @ClickHandler(slot = { 1, 0 })
    public void setHand(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
        set(EquipmentSlot.HAND, event, type -> true);
    }

    @ClickHandler(slot = { 1, 2 })
    public void setHelmet(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
        set(EquipmentSlot.HELMET, event, type -> true);
    }

    @ClickHandler(slot = { 1, 4 })
    public void setLeggings(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
        set(EquipmentSlot.LEGGINGS, event, type -> type == Material.AIR || type.name().endsWith("LEGGINGS"));
    }

    @ClickHandler(slot = { 1, 1 })
    public void setOffhand(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
        set(EquipmentSlot.OFF_HAND, event, type -> true);
    }
}
