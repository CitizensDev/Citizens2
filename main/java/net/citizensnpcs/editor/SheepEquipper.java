package net.citizensnpcs.editor;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.SheepTrait;
import net.citizensnpcs.trait.WoolColor;
import net.citizensnpcs.util.Messages;

public class SheepEquipper implements Equipper {
    @Override
    public void equip(Player equipper, NPC toEquip) {
        ItemStack hand = equipper.getInventory().getItemInMainHand();
        Sheep sheep = (Sheep) toEquip.getEntity();
        if (hand.getType() == Material.SHEARS) {
            Messaging.sendTr(equipper, toEquip.getTrait(SheepTrait.class).toggleSheared() ? Messages.SHEARED_SET
                    : Messages.SHEARED_STOPPED, toEquip.getName());
        } else if (hand.getType() == Material.INK_SACK) {
            Dye dye = (Dye) hand.getData();
            if (sheep.getColor() == dye.getColor())
                return;
            DyeColor color = dye.getColor();
            toEquip.getTrait(WoolColor.class).setColor(color);
            Messaging.sendTr(equipper, Messages.EQUIPMENT_EDITOR_SHEEP_COLOURED, toEquip.getName(),
                    color.name().toLowerCase().replace("_", " "));

            hand.setAmount(hand.getAmount() - 1);
        } else {
            toEquip.getTrait(WoolColor.class).setColor(DyeColor.WHITE);
            Messaging.sendTr(equipper, Messages.EQUIPMENT_EDITOR_SHEEP_COLOURED, toEquip.getName(), "white");
        }
        equipper.getInventory().setItemInMainHand(hand);
    }
}
