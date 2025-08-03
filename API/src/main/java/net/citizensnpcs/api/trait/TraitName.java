package net.citizensnpcs.api.trait;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A helper annotation to specify trait name for {@link TraitInfo}. Should be placed on the class implementing
 * {@link Trait}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TraitName {
    String value();
}
