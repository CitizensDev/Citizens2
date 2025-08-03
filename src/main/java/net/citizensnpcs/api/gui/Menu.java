package net.citizensnpcs.api.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;

/**
 * Defines a GUI inventory menu. Can be linked to a {@link InventoryMenuPattern} or simply at the class level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Menu {
    /**
     * The dimensions of the menu, if supported.
     */
    int[] dimensions() default { 3, 3 };

    /**
     * The click types to allow by default. Empty = all allowed
     */
    ClickType[] filter() default {};

    /**
     * The menu title.
     */
    String title() default "";

    /**
     * The inventory type.
     */
    InventoryType type() default InventoryType.CHEST;
}
