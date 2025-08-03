package net.citizensnpcs.api.gui;

import java.util.function.Consumer;
import java.util.function.Function;

public class PercentageSlotHandler implements Consumer<CitizensInventoryClickEvent> {
    private int percentage;
    private final Function<Integer, String> transformer;

    public PercentageSlotHandler(Function<Integer, String> transformer) {
        this(transformer, 100);
    }

    public PercentageSlotHandler(Function<Integer, String> transformer, int initialPercentage) {
        this.transformer = transformer;
        this.percentage = initialPercentage;
    }

    @Override
    public void accept(CitizensInventoryClickEvent event) {
        int dx = event.isShiftClick() ? 1 : 10;
        if (event.isRightClick()) {
            dx *= -1;
        }
        percentage += dx;
        if (percentage < 0) {
            percentage = 0;
        } else if (percentage > 100) {
            percentage = 100;
        }
        event.setCurrentItemDescription(transformer.apply(percentage));
        event.setCancelled(true);
    }
}
