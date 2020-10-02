package net.citizensnpcs.editor;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.util.Messages;

public class EndermanEquipper implements Equipper {
    @Override
    public void equip(Player equipper, NPC npc) {
        ItemStack hand = equipper.getInventory().getItemInHand();
        if (!hand.getType().isBlock()) {
            Messaging.sendErrorTr(equipper, Messages.EQUIPMENT_EDITOR_INVALID_BLOCK);
            return;
        }

        if (SpigotUtil.isUsing1_13API()) {
            BlockData carried = ((Enderman) npc.getEntity()).getCarriedBlock();
            if (carried == null || carried.getMaterial() == Material.AIR) {
                if (hand.getType() == Material.AIR) {
                    Messaging.sendErrorTr(equipper, Messages.EQUIPMENT_EDITOR_INVALID_BLOCK);
                    return;
                }
            } else {
                equipper.getWorld().dropItemNaturally(npc.getEntity().getLocation(),
                        new ItemStack(carried.getMaterial(), 1));
                ((Enderman) npc.getEntity()).setCarriedBlock(hand.getType().createBlockData());
                // TODO: copy block data info from itemstack?
            }

            ItemStack set = hand.clone();
            if (set.getType() != Material.AIR) {
                set.setAmount(1);
                hand.setAmount(hand.getAmount() - 1);
                equipper.getInventory().setItemInHand(hand);
            }
            npc.getOrAddTrait(Equipment.class).set(0, set);
        } else {
            MaterialData carried = ((Enderman) npc.getEntity()).getCarriedMaterial();
            if (carried.getItemType() == Material.AIR) {
                if (hand.getType() == Material.AIR) {
                    Messaging.sendErrorTr(equipper, Messages.EQUIPMENT_EDITOR_INVALID_BLOCK);
                    return;
                }
            } else {
                equipper.getWorld().dropItemNaturally(npc.getEntity().getLocation(), carried.toItemStack(1));
                ((Enderman) npc.getEntity()).setCarriedMaterial(hand.getData());
            }

            ItemStack set = hand.clone();
            if (set.getType() != Material.AIR) {
                set.setAmount(1);
                hand.setAmount(hand.getAmount() - 1);
                equipper.getInventory().setItemInHand(hand);
            }
            npc.getOrAddTrait(Equipment.class).set(0, set);
        }
    }
}
