package net.citizensnpcs.api.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

/**
 * Defines a menu transition to a new sub-menu. Can be linked to a {@link InventoryMenuTransition} or simply at the
 * class level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface MenuTransition {
    /**
     * The amount of the itemstack to display.
     */
    int amount() default 1;

    /**
     * Whitelist the allowed clicktypes for transition (empty = all allowed).
     */
    ClickType[] filter() default {};

    /**
     * The material to display (defaults to AIR). For extra customisation see {@link InventoryMenuTransition}.
     */
    Material material() default Material.AIR;

    /**
     * The position of the slot within the inventory.
     */
    int[] pos() default { 0, 0 };

    /**
     * The next sub-menu class to transition to.
     */
    Class<?> value();
}
