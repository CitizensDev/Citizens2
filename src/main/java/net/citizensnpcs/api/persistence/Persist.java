package net.citizensnpcs.api.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

import net.citizensnpcs.api.util.DataKey;

/**
 * A marker annotation for {@link PersistenceLoader} to persist a field by saving and loading it into {@link DataKey}s.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Persist {
    /**
     * The specialised collection type to use when a super class is specified. Eg. WeakHashMap.class when the field type
     * is Map.
     */
    Class<?> collectionType() default Collection.class;

    /**
     * The specific key type to use when deserialising Map keys from storage. Only supports primitive values and UUIDs
     * currently.
     */
    Class<?> keyType() default String.class;

    /**
     * If using global/static persistence, must be non-empty.
     */
    String namespace() default "";

    /**
     * Whether to use PersistenceLoader to load/save the value of this class.
     */
    boolean reify() default false;

    /**
     * Whether a value must be present at load time. If a value for the field could not be loaded,
     * {@link PersistenceLoader#load(Object, DataKey)} will return null.
     */
    boolean required() default false;

    /**
     * The DataKey path to use when saving/loading. If not present, the field name will be used instead.
     *
     * <ul>
     * <li><code>@Persist</code> -> root key + field name</li>
     * <li><code>@Persist("")</code> -> root key + "" (i.e. just the root key)</li>
     * <li><code>@Persist("sub")</code> root key + "sub"</li>
     * <li><code>@Persist("$key")</code> loads the root key, does not save</li>
     * </ul>
     */
    String value() default "UNINITIALISED";

    /**
     * The specific value type to use when deserialising values from storage. Most useful when using specific number
     * types e.g. Long, Byte, Short but storing as Integer.
     */
    Class<?> valueType() default Object.class;
}
