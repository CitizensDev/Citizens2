package net.citizensnpcs.api.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;

/**
 * Defines a slot with a certain item. Can be linked to a {@link InventoryMenuSlot} or simply at the class level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface MenuSlot {
    /**
     * The amount of the itemstack to display.
     */
    int amount() default 1;

    /**
     * Whitelist the allowed clicktypes (empty = all allowed).
     */
    ClickType[] filter() default {};

    /**
     * The material to display (defaults to AIR). For extra customisation see {@link InventoryMenuSlot}.
     */
    Material material() default Material.AIR;

    /**
     * For use with patterns.
     */
    char pat();

    /**
     * The position of the slot within the inventory.
     */
    int[] value() default { 0, 0 };
}
