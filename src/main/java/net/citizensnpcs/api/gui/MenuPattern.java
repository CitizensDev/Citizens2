package net.citizensnpcs.api.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a pattern of slots and/or transitions. Can be linked to a {@link InventoryMenuPattern} or simply at the class
 * level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD })
@Repeatable(MenuPatterns.class)
public @interface MenuPattern {
    /**
     * The offset position to start the pattern at.
     */
    int[] offset();

    MenuSlot[] slots() default {};

    MenuTransition[] transitions() default {};

    /**
     * The pattern string. 0 = AIR
     */
    String value();
}
