package net.citizensnpcs.editor;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enderman;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

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
import net.citizensnpcs.api.util.SpigotUtil;

@Menu(title = "NPC Equipment", type = InventoryType.HOPPER, dimensions = { 0, 5 })
@MenuSlot(slot = { 0, 0 }, material = Material.ENDER_PEARL, amount = 1, lore = "Place a block to hold here ->")
@MenuPattern(
        offset = { 0, 2 },
        slots = { @MenuSlot(pat = 'x', compatMaterial = { "BARRIER", "FIRE" }, title = "<4>Unused") },
        value = "xxx")
public class EndermanEquipperGUI extends InventoryMenuPage {
    @MenuSlot(slot = { 0, 1 })
    private InventoryMenuSlot hand;
    @InjectContext
    private NPC npc;

    @SuppressWarnings("deprecation")
    private Material getCarriedMaterial() {
        if (SpigotUtil.isUsing1_13API()) {
            BlockData carried = ((Enderman) npc.getEntity()).getCarriedBlock();
            return carried == null ? null : carried.getMaterial();
        } else {
            MaterialData carried = ((Enderman) npc.getEntity()).getCarriedMaterial();
            return carried == null ? null : carried.getItemType();
        }
    }

    @Override
    public void initialise(MenuContext ctx) {
        Material mat = getCarriedMaterial();
        if (mat == null)
            return;
        hand.setItemStack(new ItemStack(mat, 1));
    }

    @ClickHandler(slot = { 0, 1 }, filter = { InventoryAction.PICKUP_ALL, InventoryAction.PLACE_ALL })
    public void setHand(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL && event.getCursor() != null) {
            event.setResult(Result.DENY);
            return;
        }
        if (event.getAction() == InventoryAction.PLACE_ALL && (event.getCurrentItem() != null
                || !event.getCursor().getType().isBlock() || event.getCursorNonNull().getAmount() > 1)) {
            event.setResult(Result.DENY);
            return;
        }
        if (SpigotUtil.isUsing1_13API()) {
            ((Enderman) npc.getEntity()).setCarriedBlock(
                    event.getAction() == InventoryAction.PLACE_ALL ? event.getResultItem().getType().createBlockData()
                            : null);
        } else {
            ((Enderman) npc.getEntity()).setCarriedMaterial(
                    event.getAction() == InventoryAction.PLACE_ALL ? event.getResultItem().getData() : null);
        }
    }
}
