package net.citizensnpcs.api.npc.trait;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SaveId {

    /**
     * ID used for trait and character saving
     */
    public String value();
}