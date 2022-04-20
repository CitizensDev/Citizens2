package net.citizensnpcs.editor;

import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryAction;
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
import net.citizensnpcs.trait.Saddle;

@Menu(title = "NPC Equipment", type = InventoryType.HOPPER, dimensions = { 0, 5 })
@MenuSlot(slot = { 0, 0 }, material = Material.SADDLE, amount = 1, lore = "Place a saddle here ->")
@MenuPattern(
        offset = { 0, 2 },
        slots = { @MenuSlot(pat = 'x', compatMaterial = { "BARRIER", "FIRE" }, title = "<4>Unused") },
        value = "xxx")
public class SteerableEquipperGUI extends InventoryMenuPage {
    @InjectContext
    private NPC npc;
    @MenuSlot(slot = { 0, 1 })
    private InventoryMenuSlot saddle;

    @Override
    public void initialise(MenuContext ctx) {
        Saddle trait = npc.getOrAddTrait(Saddle.class);
        if (trait.useSaddle()) {
            saddle.setItemStack(new ItemStack(Material.SADDLE, 1));
        }
    }

    @ClickHandler(slot = { 0, 1 }, filter = { InventoryAction.PICKUP_ALL, InventoryAction.PLACE_ALL })
    public void setSaddle(InventoryMenuSlot slot, CitizensInventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL && event.getCursor() != null) {
            event.setResult(Result.DENY);
            return;
        }
        if (event.getAction() == InventoryAction.PLACE_ALL
                && (event.getCurrentItem() != null || event.getCursor().getType() != Material.SADDLE)) {
            event.setResult(Result.DENY);
            return;
        }
        npc.getOrAddTrait(Saddle.class).toggle();
    }
}
