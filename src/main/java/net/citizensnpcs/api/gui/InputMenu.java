package net.citizensnpcs.api.gui;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Function;

@Menu(type = InventoryType.ANVIL)
public class InputMenu extends InventoryMenuPage {
    private final Function<String, Boolean> callback;
    private MenuContext ctx;
    private final Supplier<String> initialValue;
    @MenuSlot(slot = { 0, 0 }, material = Material.PAPER, amount = 1)
    private InventoryMenuSlot slot;

    public InputMenu(Supplier<String> initialValue, Function<String, Boolean> callback) {
        this.initialValue = initialValue;
        this.callback = callback;
    }

    @ClickHandler(slot = { 0, 0 })
    public void cancel(InventoryMenuSlot slot, CitizensInventoryClickEvent evt) {
        evt.setCancelled(true);
        ctx.getMenu().transitionBack();
    }

    @Override
    public void initialise(MenuContext ctx) {
        this.ctx = ctx;
        ItemStack item = slot.getCurrentItem();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(initialValue.get());
        item.setItemMeta(meta);
    }

    @ClickHandler(slot = { 0, 2 })
    public void save(InventoryMenuSlot slot, CitizensInventoryClickEvent evt) {
        evt.setCancelled(true);
        if (callback.apply(slot.getCurrentItem().getItemMeta().getDisplayName())) {
            ctx.getMenu().transitionBack();
        } else {
            evt.setCancelled(true);
        }
    }

    public static InputMenu setter(Supplier<String> initialValue, Consumer<String> callback) {
        return new InputMenu(initialValue, (s) -> {
            callback.accept(s);
            return true;
        });
    }
}
