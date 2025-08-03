package net.citizensnpcs.editor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;

public class WolfEquipper implements Equipper {
    @Override
    public void equip(Player equipper, NPC toEquip) {
        ItemStack hand = equipper.getInventory().getItemInHand();
        ItemStack equipped = toEquip.getOrAddTrait(Equipment.class).get(EquipmentSlot.BODY);
        if ((hand == null || hand.getType() == Material.AIR) && equipped != null) {
            toEquip.getOrAddTrait(Equipment.class).set(EquipmentSlot.BODY, null);
            equipper.getWorld().dropItem(equipper.getLocation(), equipped);
            return;
        }
        if (hand.getType().name().equals("WOLF_ARMOR")) {
            ItemStack armor = hand.clone();
            hand.setAmount(hand.getAmount() - 1);
            armor.setAmount(1);
            toEquip.getOrAddTrait(Equipment.class).set(EquipmentSlot.BODY, armor);
            equipper.getInventory().setItemInHand(hand);
        }
    }
}
