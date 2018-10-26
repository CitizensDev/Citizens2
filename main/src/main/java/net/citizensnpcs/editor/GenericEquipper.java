package net.citizensnpcs.editor;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.SpigotUtil;
import net.citizensnpcs.util.Messages;

public class GenericEquipper implements Equipper {
    @Override
    public void equip(Player equipper, NPC toEquip) {
        ItemStack hand = equipper.getInventory().getItemInHand();
        Equipment trait = toEquip.getTrait(Equipment.class);
        EquipmentSlot slot = EquipmentSlot.HAND;
        Material type = hand == null ? Material.AIR : hand.getType();
        // First, determine the slot to edit
        if (type.name().equals("ELYTRA") && !equipper.isSneaking()) {
            slot = EquipmentSlot.CHESTPLATE;
        } else {
            if (HELMETS.contains(type)) {
                if (!equipper.isSneaking()) {
                    slot = EquipmentSlot.HELMET;
                }
            } else if (CHESTPLATES.contains(type)) {
                if (!equipper.isSneaking()) {
                    slot = EquipmentSlot.CHESTPLATE;
                }
            } else if (LEGGINGS.contains(type)) {
                if (!equipper.isSneaking()) {
                    slot = EquipmentSlot.LEGGINGS;
                }
            } else if (BOOTS.contains(type)) {
                if (!equipper.isSneaking()) {
                    slot = EquipmentSlot.BOOTS;
                }
            } else if (type == Material.AIR) {
                if (equipper.isSneaking()) {
                    for (int i = 0; i < 6; i++) {
                        if (trait.get(i) != null && trait.get(i).getType() != Material.AIR) {
                            equipper.getWorld().dropItemNaturally(toEquip.getEntity().getLocation(), trait.get(i));
                            trait.set(i, null);
                        }
                    }
                    Messaging.sendTr(equipper, Messages.EQUIPMENT_EDITOR_ALL_ITEMS_REMOVED, toEquip.getName());
                } else {
                    return;
                }
            }
        }

        // Drop any previous equipment on the ground
        ItemStack equippedItem = trait.get(slot);
        if (equippedItem != null && equippedItem.getType() != Material.AIR) {
            equipper.getWorld().dropItemNaturally(toEquip.getEntity().getLocation(), equippedItem);
        }

        // Now edit the equipment based on the slot
        if (type != Material.AIR) {
            // Set the proper slot with one of the item
            ItemStack clone = hand.clone();
            clone.setAmount(1);
            trait.set(slot, clone);
            hand.setAmount(hand.getAmount() - 1);
            equipper.getInventory().setItemInHand(hand);
        }
    }

    private static Set<Material> BOOTS = EnumSet.of(Material.CHAINMAIL_BOOTS, Material.DIAMOND_BOOTS,
            Material.IRON_BOOTS, Material.LEATHER_BOOTS,
            SpigotUtil.isUsing1_13API() ? Material.GOLDEN_BOOTS : Material.valueOf("GOLD_BOOTS"));
    private static Set<Material> CHESTPLATES = EnumSet.of(Material.CHAINMAIL_CHESTPLATE, Material.DIAMOND_CHESTPLATE,
            Material.IRON_CHESTPLATE, Material.LEATHER_CHESTPLATE,
            SpigotUtil.isUsing1_13API() ? Material.GOLDEN_CHESTPLATE : Material.valueOf("GOLD_CHESTPLATE"));
    private static Set<Material> HELMETS = SpigotUtil.isUsing1_13API()
            ? EnumSet.of(Material.PUMPKIN, Material.JACK_O_LANTERN, Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET,
                    Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.TURTLE_HELMET, Material.GOLDEN_HELMET,
                    Material.CREEPER_HEAD, Material.DRAGON_HEAD, Material.PLAYER_HEAD, Material.SKELETON_SKULL,
                    Material.ZOMBIE_HEAD, Material.WITHER_SKELETON_SKULL)
            : EnumSet.of(Material.PUMPKIN, Material.JACK_O_LANTERN, Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET,
                    Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.valueOf("SKULL_ITEM"),
                    Material.valueOf("GOLD_HELMET"));
    private static Set<Material> LEGGINGS = EnumSet.of(Material.CHAINMAIL_LEGGINGS, Material.DIAMOND_LEGGINGS,
            Material.IRON_LEGGINGS, Material.LEATHER_LEGGINGS,
            SpigotUtil.isUsing1_13API() ? Material.GOLDEN_LEGGINGS : Material.valueOf("GOLD_LEGGINGS"));
}
