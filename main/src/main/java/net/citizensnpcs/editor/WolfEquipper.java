package net.citizensnpcs.editor;

import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;

public class WolfEquipper implements Equipper {
    @Override
    public void equip(Player equipper, NPC toEquip) {
        ItemStack hand = equipper.getInventory().getItemInHand();
        Wolf wolf = (Wolf) toEquip.getEntity();
        if (hand.getType().name().equals("WOLF_ARMOR")) {
            ItemStack armor = hand.clone();
            hand.setAmount(hand.getAmount() - 1);
            armor.setAmount(1);
            wolf.getEquipment().setChestplate(armor);
            equipper.getInventory().setItemInHand(hand);
        }
    }
}
