package net.citizensnpcs.editor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Equipable {
    public void equip(Player equipper, ItemStack toEquip);
}