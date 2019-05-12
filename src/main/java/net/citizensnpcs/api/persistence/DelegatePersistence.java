package net.citizensnpcs.api.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Delegates persistence to a given {@link Persister}, which will be used to create and save instances.
 *
 * @see Persist
 * @see Persister
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DelegatePersistence {
    Class<? extends Persister<?>> value();
}
