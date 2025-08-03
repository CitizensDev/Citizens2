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
import net.citizensnpcs.util.Util;

public class SheepEquipper implements Equipper {
    @Override
    public void equip(Player equipper, NPC toEquip) {
        ItemStack hand = equipper.getInventory().getItemInHand();
        Sheep sheep = (Sheep) toEquip.getEntity();
        if (hand.getType() == Material.SHEARS) {
            Messaging.sendTr(equipper, toEquip.getOrAddTrait(SheepTrait.class).toggleSheared() ? Messages.SHEARED_SET
                    : Messages.SHEARED_STOPPED, toEquip.getName());
        } else if (hand.getType() != null && hand.getType().name().contains("INK_SAC")) {
            Dye dye = (Dye) hand.getData();
            if (sheep.getColor() == dye.getColor())
                return;
            DyeColor color = dye.getColor();
            toEquip.getOrAddTrait(WoolColor.class).setColor(color);
            Messaging.sendTr(equipper, Messages.EQUIPMENT_EDITOR_SHEEP_COLOURED, toEquip.getName(),
                    Util.prettyEnum(color));

            hand.setAmount(hand.getAmount() - 1);
        } else {
            toEquip.getOrAddTrait(WoolColor.class).setColor(DyeColor.WHITE);
            Messaging.sendTr(equipper, Messages.EQUIPMENT_EDITOR_SHEEP_COLOURED, toEquip.getName(), "white");
        }
        equipper.getInventory().setItemInHand(hand);
    }
}
