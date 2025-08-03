package net.citizensnpcs.api.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Annotates a method as a click handler that will accept inventory click events. Currently, each listener must take
 * {@link InventoryMenuSlot} and {@link InventoryClickEvent} arguments.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Repeatable(ClickHandlers.class)
public @interface ClickHandler {
    /**
     * An optional filter for specific actions. Default = handle all clicks
     */
    InventoryAction[] filter() default {};

    /**
     * The slot position to handle clicks for.
     */
    int[] slot();
}
