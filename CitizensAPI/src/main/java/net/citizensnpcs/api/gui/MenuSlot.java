package net.citizensnpcs.api.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.bukkit.Material;

/**
 * Defines a slot with a certain item. Can be linked to a {@link InventoryMenuSlot} or simply at the class level.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD })
@Repeatable(MenuSlots.class)
public @interface MenuSlot {
    /**
     * The amount of the itemstack to display.
     */
    int amount() default 1;

    /**
     * For compatibility with old Minecraft versions - a two String array {intended material name, fallback material
     * name} e.g. {"SHIELD", "AIR"}
     */
    String[] compatMaterial() default {};

    /**
     * The lore of the inventory item, newline-delimited.
     */
    String lore() default "EMPTY";

    /**
     * The material to display (defaults to AIR). For extra customisation see {@link InventoryMenuSlot}.
     */
    Material material() default Material.AIR;

    /**
     * For use with patterns.
     */
    char pat() default '0';

    /**
     * The position of the slot within the inventory.
     */
    int[] slot() default { 0, 0 };

    /**
     * The display name of the inventory item.
     */
    String title() default "EMPTY";
}
