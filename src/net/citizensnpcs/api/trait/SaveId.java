package net.citizensnpcs.api.trait;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation determines the key under which to save a character or trait.
 * In order for a character or trait to be saved, the class must contain this
 * annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SaveId {

    /**
     * ID used for trait and character saving
     */
    public String value();
}