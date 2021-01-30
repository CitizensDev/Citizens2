package net.citizensnpcs.editor;

import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

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
@MenuSlot(slot = { 0, 2 }, material = Material.DIAMOND_HELMET, amount = 1, filter = InventoryAction.UNKNOWN)
@MenuSlot(slot = { 0, 4 }, material = Material.DIAMOND_LEGGINGS, amount = 1, filter = InventoryAction.UNKNOWN)
@MenuSlot(slot = { 0, 1 }, material = Material.ELYTRA, amount = 1, filter = InventoryAction.UNKNOWN)
@MenuSlot(slot = { 0, 0 }, material = Material.DIAMOND_SWORD, amount = 1, filter = InventoryAction.UNKNOWN)
@MenuSlot(slot = { 0, 3 }, material = Material.DIAMOND_CHESTPLATE, amount = 1, filter = InventoryAction.UNKNOWN)
@MenuSlot(slot = { 0, 5 }, material = Material.DIAMOND_BOOTS, amount = 1, filter = InventoryAction.UNKNOWN)
@MenuPattern(offset = { 0, 6 }, slots = { @MenuSlot(filter = InventoryAction.UNKNOWN, pat = 'x') }, value = "xxx\nxxx")
public class EquipmentGUI extends InventoryMenuPage {
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

    // TODO: move into API?
    private ItemStack getResult(InventoryClickEvent event) {
        ItemStack stack = event.getCurrentItem() == null ? new ItemStack(event.getCursor().getType(), 0)
                : event.getCurrentItem().clone();
        switch (event.getAction()) {
            case PICKUP_ONE:
                stack.setAmount(stack.getAmount() - 1);
                break;
            case PICKUP_HALF:
                stack.setAmount((int) Math.floor(stack.getAmount() / 2.0));
                break;
            case PICKUP_ALL:
                stack = null;
                break;
            case PLACE_ALL:
                stack.setAmount(
                        Math.min(stack.getAmount() + event.getCursor().getAmount(), stack.getType().getMaxStackSize()));
                break;
            case PLACE_SOME:
                stack.setAmount(Math.min(stack.getAmount(), stack.getType().getMaxStackSize()));
                break;
            case PLACE_ONE:
                stack.setAmount(stack.getAmount() + 1);
                break;
            default:
                event.setCancelled(true);
                event.setResult(Result.DENY);
                return null;
        }
        return stack;
    }

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

    private void set(EquipmentSlot slot, InventoryClickEvent event, Function<Material, Boolean> filter) {
        ItemStack result = getResult(event);
        if (event.isCancelled() || (result != null && !filter.apply(result.getType()))) {
            event.setResult(Result.DENY);
            return;
        }
        npc.getOrAddTrait(Equipment.class).set(slot, result);
    }

    @ClickHandler(slot = { 1, 5 })
    public void setBoots(InventoryMenuSlot slot, InventoryClickEvent event) {
        set(EquipmentSlot.BOOTS, event, (type) -> type.name().endsWith("BOOTS"));
    }

    @ClickHandler(slot = { 1, 3 })
    public void setChest(InventoryMenuSlot slot, InventoryClickEvent event) {
        set(EquipmentSlot.CHESTPLATE, event,
                (type) -> type.name().endsWith("CHESTPLATE") || type.name().equals("ELYTRA"));
    }

    @ClickHandler(slot = { 1, 0 })
    public void setHand(InventoryMenuSlot slot, InventoryClickEvent event) {
        set(EquipmentSlot.HAND, event, (type) -> true);
    }

    @ClickHandler(slot = { 1, 2 })
    public void setHelmet(InventoryMenuSlot slot, InventoryClickEvent event) {
        set(EquipmentSlot.HELMET, event, (type) -> true);
    }

    @ClickHandler(slot = { 1, 4 })
    public void setLeggings(InventoryMenuSlot slot, InventoryClickEvent event) {
        set(EquipmentSlot.LEGGINGS, event, (type) -> type.name().endsWith("LEGGINGS"));
    }

    @ClickHandler(slot = { 1, 1 })
    public void setOffhand(InventoryMenuSlot slot, InventoryClickEvent event) {
        set(EquipmentSlot.OFF_HAND, event, (type) -> true);
    }
}
