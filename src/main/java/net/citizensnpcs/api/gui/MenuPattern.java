package net.citizensnpcs.api.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a pattern of slots. Can be linked to a {@link InventoryMenuPattern} or simply at the class level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface MenuPattern {
    /**
     * The offset position to start the pattern at.
     */
    int[] offset();

    /**
     * The pattern string. 0 = AIR
     */
    String value();
}
