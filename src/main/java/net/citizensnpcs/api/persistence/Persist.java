package net.citizensnpcs.api.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

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

    /**
     * The specialised collection type to use when a super class is specified.
     * Eg. WeakHashMap.class when the field type is Map.
     */
    Class<? super Collection<?>> collectionType() default Collection.class;
}
