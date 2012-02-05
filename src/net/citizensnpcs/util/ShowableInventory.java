package net.citizensnpcs.util;

import net.minecraft.server.EntityHuman;
import net.minecraft.server.IInventory;
import net.minecraft.server.ItemStack;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ShowableInventory implements IInventory {
    private final ItemStack[] contents;
    private final Inventory inventory = new CraftInventory(this);
    private final String name;

    public ShowableInventory(String name, int size) {
        this.name = name;
        this.contents = new ItemStack[size];
    }

    @Override
    public int getSize() {
        return contents.length;
    }

    @Override
    public ItemStack getItem(int i) {
        return contents[i];
    }

    @Override
    public ItemStack splitStack(int i, int j) {
        if (contents[i].count <= j) {
            ItemStack itemstack = contents[i];
            contents[i] = null;
            return itemstack;
        } else {
            ItemStack itemstack = contents[i].a(j);
            if (contents[i].count == 0) {
                contents[i] = null;
            }
            return itemstack;
        }
    }

    @Override
    public void setItem(int i, ItemStack itemstack) {
        contents[i] = itemstack;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void update() {
    }

    @Override
    public boolean a(EntityHuman entityhuman) {
        return true; // always keep showing ?
    }

    @Override
    public void f() {
    }

    @Override
    public void g() {
    }

    @Override
    public ItemStack[] getContents() {
        return contents;
    }

    public Inventory asInventory() {
        return inventory;
    }

    public void show(Player player) {
        ((CraftPlayer) player).getHandle().a(this);
    }

    public static Inventory create(String name, int size) {
        return new ShowableInventory(name, size).asInventory();
    }
}
