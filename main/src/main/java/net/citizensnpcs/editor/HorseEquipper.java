package net.citizensnpcs.editor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Inventory;

public class HorseEquipper implements Equipper {
    @Override
    public void equip(Player equipper, NPC toEquip) {
        toEquip.getOrAddTrait(Inventory.class);
        equipper.openInventory(((InventoryHolder) toEquip.getEntity()).getInventory());
    }
}
