package net.citizensnpcs.api.persistence;

import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.ItemStorage;

import org.bukkit.inventory.ItemStack;

public class ItemStackPersister implements Persister<ItemStack> {
    @Override
    public ItemStack create(DataKey root) {
        return ItemStorage.loadItemStack(root);
    }

    @Override
    public void save(ItemStack instance, DataKey root) {
        ItemStorage.saveItem(root, instance);
    }
}
