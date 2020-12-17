package net.citizensnpcs.api.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bukkit.Material;

/**
 * Defines a pattern of slots. Can be linked to a {@link InventoryMenuPattern} or simply at the class level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface MenuPattern {
    /**
     * The amount of the itemstacks to display.
     */
    int amount() default 1;

    /**
     * The material to display (defaults to AIR).
     */
    Material material() default Material.AIR;

    /**
     * The pattern string. 0 = AIR
     */
    String value();
}
