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
     * The specialised collection type to use when a super class is specified.
     * Eg. WeakHashMap.class when the field type is Map.
     */
    Class<?> collectionType() default Collection.class;

    /**
     * Whether a value must be present at load time. If a value for the field
     * could not be loaded, {@link PersistenceLoader#load(Object, DataKey)} will
     * return null.
     */
    boolean required() default false;

    /**
     * The save key to use when saving. If not present, the field name will be
     * used instead.
     * 
     * <ul>
     * <li><code>@Persist</code> -> root key + field name</li>
     * <li><code>@Persist("")</code> -> root key + "" (or simply root key)</li>
     * <li><code>@Persist("sub")</code> root key + "sub"</li>
     * </ul>
     */
    String value() default "UNINITIALISED";
}
