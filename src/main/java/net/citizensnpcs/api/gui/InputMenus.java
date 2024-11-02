package net.citizensnpcs.api.gui;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.gui.InputMenus.Choice.Type;
import net.citizensnpcs.api.util.Messaging;

public class InputMenus {
    public static class BooleanSlotHandler implements Consumer<CitizensInventoryClickEvent> {
        private final Function<Boolean, String> transformer;
        private boolean value;

        public BooleanSlotHandler(Function<Boolean, String> transformer) {
            this(transformer, false);
        }

        public BooleanSlotHandler(Function<Boolean, String> transformer, boolean initial) {
            this.transformer = transformer;
            this.value = initial;
        }

        @Override
        public void accept(CitizensInventoryClickEvent event) {
            value = !value;
            event.setCurrentItemDescription(transformer.apply(value));
            event.setResult(Result.DENY);
        }
    }

    public static class Choice<T> {
        private boolean active;
        private String description;
        private Material material;
        private T value;

        public ItemStack createDisplayItem() {
            ItemStack item = new ItemStack(getDisplayMaterial(), 1);
            ItemMeta meta = item.getItemMeta();
            if (getDescription().contains("\n")) {
                String[] parts = getDescription().split("\n", 2);
                meta.setDisplayName(parts[0]);
                meta.setLore(Arrays.asList(parts[1].split("\n")));
            } else if (getValue() instanceof Enum) {
                String name = ((Enum<?>) getValue()).name();
                meta.setDisplayName(name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT));
                meta.setLore(Arrays.asList(getDescription().split("\n")));
            }
            meta.setDisplayName((isActive() ? ChatColor.GREEN : ChatColor.RED) + meta.getDisplayName());
            item.setItemMeta(meta);
            return item;
        }

        public String getDescription() {
            return Messaging.parseComponents(description);
        }

        public Material getDisplayMaterial() {
            return material;
        }

        public T getValue() {
            return value;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public enum Type {
            PICKER,
            TOGGLE;
        }

        public static <T> Choice<T> of(T value, Material display, String description, boolean active) {
            Choice<T> ret = new Choice<>();
            ret.active = active;
            ret.material = display;
            ret.value = value;
            ret.description = description;
            return ret;
        }
    }

    @Menu(type = InventoryType.CHEST)
    private static class ChoiceInputMenu<T> extends InventoryMenuPage {
        private final Consumer<List<Choice<T>>> callback;
        private final Choice<T>[] choices;
        private final String title;
        private final Type type;

        public ChoiceInputMenu(String title, Choice.Type type, Consumer<List<Choice<T>>> callback,
                Choice<T>[] choices) {
            this.title = title;
            this.callback = callback;
            this.choices = choices;
            this.type = type;
        }

        @Override
        public Inventory createInventory(String title) {
            if (choices.length <= 3)
                return Bukkit.createInventory(null, InventoryType.HOPPER, this.title);
            else
                return Bukkit.createInventory(null, Math.min(54, choices.length / 5 * 9 + 9), this.title);
        }

        @Override
        public void initialise(final MenuContext ctx) {
            for (int i = 0; i < choices.length; i++) {
                final Choice<T> choice = choices[i];
                final InventoryMenuSlot slot = ctx.getSlot(i * 2);
                slot.setItemStack(choice.createDisplayItem());
                slot.setClickHandler(evt -> {
                    evt.setCancelled(true);
                    boolean newState = !choice.isActive();
                    switch (type) {
                        case TOGGLE:
                            choice.setActive(newState);
                            slot.setItemStack(choice.createDisplayItem());
                            break;
                        case PICKER:
                            for (int j = 0; j < choices.length; j++) {
                                choices[j].setActive(false);
                            }
                            choice.setActive(true);
                            ctx.getMenu().transitionBack();
                            break;
                    }
                });
            }
        }

        @Override
        public void onClose(HumanEntity entity) {
            List<Choice<T>> ret = Lists.newArrayListWithExpectedSize(choices.length);
            for (Choice<T> choice : choices) {
                if (choice.isActive()) {
                    ret.add(choice);
                }
            }
            callback.accept(ret);
        }
    }

    @Menu(type = InventoryType.ANVIL)
    private static class StringInputMenu extends InventoryMenuPage {
        private final Function<String, Boolean> callback;
        private MenuContext ctx;
        @MenuSlot(slot = { 0, 0 }, material = Material.PAPER, amount = 1)
        private InventoryMenuSlot from;
        private final Supplier<String> initialValue;

        public StringInputMenu(Supplier<String> initialValue, Function<String, Boolean> callback) {
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
            ItemStack item = from.getCurrentItem();
            ItemMeta meta = item.getItemMeta();
            String name = initialValue.get();
            meta.setDisplayName(name == null ? "Not set" : name);
            item.setItemMeta(meta);
        }

        @ClickHandler(slot = { 0, 2 })
        public void save(InventoryMenuSlot slot, CitizensInventoryClickEvent evt) {
            evt.setCancelled(true);
            String res = slot.getCurrentItem() != null && slot.getCurrentItem().getItemMeta() != null
                    ? slot.getCurrentItem().getItemMeta().getDisplayName()
                    : null;
            if (res != null && (res.isEmpty() || res.equalsIgnoreCase("Not set") || res.equalsIgnoreCase("null"))) {
                res = null;
            }
            if (callback.apply(res)) {
                ctx.getMenu().transitionBack();
            }
        }
    }

    public static BooleanSlotHandler clickToggle(Function<Boolean, String> transformer, boolean initialValue) {
        return new BooleanSlotHandler(transformer, initialValue);
    }

    public static InventoryMenuPage filteredStringSetter(Supplier<String> initialValue,
            Function<String, Boolean> callback) {
        return new StringInputMenu(initialValue, callback);
    }

    @SuppressWarnings("unchecked")
    public static <T> InventoryMenuPage picker(String title, Consumer<Choice<T>> callback,
            InputMenus.Choice<T>... choices) {
        return new ChoiceInputMenu<>(title, Choice.Type.PICKER, chosen -> {
            callback.accept(chosen.size() > 0 ? chosen.get(0) : null);
        }, choices);
    }

    public static void runChatStringSetter(InventoryMenu menu, HumanEntity viewer, String description,
            Consumer<String> callback) {
        menu.close(viewer);
        Messaging.send(viewer, description);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(ignoreCancelled = true)
            public void onPlayerChat(AsyncPlayerChatEvent event) {
                HandlerList.unregisterAll(this);
                String chat = event.getMessage();
                event.setCancelled(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
                    callback.accept(chat);
                    menu.present(viewer);
                });
            }
        }, CitizensAPI.getPlugin());
    }

    public static InventoryMenuPage stringSetter(Supplier<String> initialValue, Consumer<String> callback) {
        return new StringInputMenu(initialValue, s -> {
            callback.accept(s);
            return true;
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> InventoryMenuPage toggle(String title, Consumer<List<Choice<T>>> callback,
            InputMenus.Choice<T>... choices) {
        return new ChoiceInputMenu<>(title, Choice.Type.TOGGLE, callback, choices);
    }

    public static BooleanSlotHandler toggler(Consumer<Boolean> consumer, boolean initialValue) {
        return new BooleanSlotHandler(b -> {
            consumer.accept(b);
            return b ? ChatColor.GREEN + "On" : ChatColor.RED + "Off";
        }, initialValue);
    }
}
