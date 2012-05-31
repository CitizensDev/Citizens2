package net.citizensnpcs.abstraction.bukkit;

import net.citizensnpcs.api.abstraction.Equipment;
import net.citizensnpcs.api.abstraction.ItemStack;
import net.citizensnpcs.api.abstraction.entity.Player;

import org.bukkit.Bukkit;

public class BukkitPlayer extends BukkitLivingEntity implements Player {
    public BukkitPlayer(org.bukkit.entity.Player entity) {
        super(entity);
    }

    @Override
    public String getName() {
        return getPlayer().getName();
    }

    @Override
    public boolean hasPermission(String perm) {
        return getPlayer().hasPermission(perm);
    }

    @Override
    public void sendMessage(String message) {
        getPlayer().sendMessage(message);
    }

    @Override
    public void useCommand(String cmd) {
        Bukkit.getServer().dispatchCommand(getPlayer(), cmd);
    }

    @Override
    public ItemStack getEquipment(Equipment slot) {
        switch (slot) {
        case CARRIED:
            return BukkitConverter.toItemStack(getPlayer().getItemInHand());
        case HELMET:
            return BukkitConverter.toItemStack(getPlayer().getInventory().getHelmet());
        case BOOTS:
            return BukkitConverter.toItemStack(getPlayer().getInventory().getBoots());
        case CHESTPLATE:
            return BukkitConverter.toItemStack(getPlayer().getInventory().getChestplate());
        case LEGGINGS:
            return BukkitConverter.toItemStack(getPlayer().getInventory().getLeggings());
        default:
            return null;
        }
    }

    @Override
    public void setEquipment(Equipment slot, ItemStack item) {
        switch (slot) {
        case CARRIED:
            getPlayer().setItemInHand(BukkitConverter.fromItemStack(item));
        case HELMET:
            getPlayer().getInventory().setHelmet(BukkitConverter.fromItemStack(item));
        case BOOTS:
            getPlayer().getInventory().setBoots(BukkitConverter.fromItemStack(item));
        case CHESTPLATE:
            getPlayer().getInventory().setChestplate(BukkitConverter.fromItemStack(item));
        case LEGGINGS:
            getPlayer().getInventory().setLeggings(BukkitConverter.fromItemStack(item));
        }
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isOnline();
    }

    @Override
    public void setArmor(ItemStack[] armor) {
        org.bukkit.inventory.ItemStack[] stacks = new org.bukkit.inventory.ItemStack[armor.length];
        for (int i = 0; i < armor.length; ++i) {
            stacks[i] = BukkitConverter.fromItemStack(armor[i]);
        }
        getPlayer().getInventory().setArmorContents(stacks);
    }

    private org.bukkit.entity.Player getPlayer() {
        return (org.bukkit.entity.Player) entity;
    }
}
