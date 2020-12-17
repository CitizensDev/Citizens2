package net.citizensnpcs.api.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bukkit.event.inventory.ClickType;

/**
 * Defines a click handler.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface ClickHandler {
    /**
     * An optional filter for specific click types.
     */
    ClickType[] value() default {};
}
