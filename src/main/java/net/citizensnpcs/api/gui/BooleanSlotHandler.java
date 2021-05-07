package net.citizensnpcs.api.gui;

import java.util.function.Consumer;

import org.bukkit.event.Event.Result;

import com.google.common.base.Function;

public class BooleanSlotHandler implements Consumer<CitizensInventoryClickEvent> {
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
