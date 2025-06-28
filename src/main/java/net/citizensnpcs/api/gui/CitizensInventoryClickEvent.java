package net.citizensnpcs.api.gui;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.citizensnpcs.api.util.Messaging;

public class CitizensInventoryClickEvent extends InventoryClickEvent {
    private final InventoryClickEvent event;
    private final ItemStack result;

    public CitizensInventoryClickEvent(InventoryClickEvent event, int pickupAmount) {
        super(event.getView(), event.getSlotType(), event.getSlot(), event.getClick(), event.getAction(),
                event.getHotbarButton());
        this.event = event;
        this.result = getResult(event, pickupAmount);
    }

    @Override
    public ItemStack getCurrentItem() {
        return event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR ? null
                : event.getCurrentItem();
    }

    public ItemStack getCurrentItemNonNull() {
        return event.getCurrentItem() == null ? new ItemStack(Material.AIR, 0) : event.getCurrentItem();
    }

    @Override
    public ItemStack getCursor() {
        return event.getCursor() == null || event.getCursor().getType() == Material.AIR ? null : event.getCursor();
    }

    public ItemStack getCursorNonNull() {
        return event.getCursor() == null ? new ItemStack(Material.AIR, 0) : event.getCursor();
    }

    @Override
    public Result getResult() {
        return event.getResult();
    }

    private ItemStack getResult(InventoryClickEvent event, int pickupAmount) {
        ItemStack stack = event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR
                ? event.getCursor().clone()
                : event.getCurrentItem().clone();
        int formerAmount = event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR ? 0
                : event.getCurrentItem().getAmount();
        switch (event.getAction()) {
            case PICKUP_ONE:
                stack.setAmount(formerAmount - 1);
                break;
            case PICKUP_SOME:
                stack.setAmount(formerAmount - pickupAmount);
                break;
            case PICKUP_HALF:
                stack.setAmount((int) Math.floor(formerAmount / 2.0));
                break;
            case PICKUP_ALL:
                stack = null;
                break;
            case PLACE_ALL:
            case PLACE_SOME:
                stack.setAmount(
                        Math.min(formerAmount + event.getCursor().getAmount(), stack.getType().getMaxStackSize()));
                break;
            case PLACE_ONE:
                stack.setAmount(Math.min(formerAmount + 1, stack.getType().getMaxStackSize()));
                break;
            default:
                event.setCancelled(true);
                return null;
        }
        return stack;
    }

    public ItemStack getResultItem() {
        return result;
    }

    public ItemStack getResultItemNonNull() {
        return result == null ? new ItemStack(Material.AIR, 0) : result;
    }

    @Override
    public boolean isCancelled() {
        return event.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        event.setCancelled(cancel);
    }

    @Override
    public void setCurrentItem(ItemStack item) {
        event.setCurrentItem(item);
    }

    public void setCurrentItemDescription(String description) {
        ItemMeta meta = getCurrentItem().getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setLore(Arrays.asList(Messaging.parseComponents(description).split("\n")));
        event.getCurrentItem().setItemMeta(meta);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setCursor(ItemStack cursor) {
        event.setCursor(cursor);
    }

    @Override
    public void setResult(Result result) {
        event.setResult(result);
    }
}
