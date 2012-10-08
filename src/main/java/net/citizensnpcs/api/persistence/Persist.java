package net.citizensnpcs.api.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.citizensnpcs.api.util.DataKey;

/**
 * Tells the {@link PersistenceLoader} to persist this field by saving and
 * loading it into {@link DataKey}s.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Persist {
    /**
     * The save key to use when saving. If not present, the field name will be
     * used instead.
     */
    String value() default "";
}
