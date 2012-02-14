package net.citizensnpcs.api.npc.trait;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SaveId {

    /**
     * ID used for trait and character saving
     */
    public String value();
}