package net.citizensnpcs.api.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bukkit.event.inventory.ClickType;

/**
 * Defines a menu transition to a new sub-menu. Can be linked to a {@link InventoryMenuTransition} or simply at the
 * class level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD })
@Repeatable(MenuTransitions.class)
public @interface MenuTransition {
    /**
     * Whitelist the allowed clicktypes for transition (empty = all allowed).
     */
    ClickType[] filter() default {};

    /**
     * For use with patterns.
     */
    char pat() default '0';

    /**
     * The position of the slot within the inventory.
     */
    int[] pos() default { 0, 0 };

    /**
     * The next sub-menu class to transition to.
     */
    Class<? extends InventoryMenuPage> value();
}
