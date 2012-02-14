package net.citizensnpcs.npc;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.StringHelper;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.IInventory;
import net.minecraft.server.ItemStack;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class NPCInventory implements IInventory {
    private final int size = 36;
    private final ItemStack[] contents;
    private final Inventory inventory = new CraftInventory(this);
    private String name;

    public NPCInventory(NPC npc) {
        name = StringHelper.parseColors(npc.getFullName());
        contents = new ItemStack[size];
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
            if (contents[i].count == 0)
                contents[i] = null;
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
        return true;
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

    public void setName(String name) {
        this.name = StringHelper.parseColors(name);
    }
}