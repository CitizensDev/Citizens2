package net.citizensnpcs.api.trait;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;

import net.citizensnpcs.api.npc.NPC;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TraitEventHandler {
    Class<? extends NPCEventExtractor> processor() default NPCEventExtractor.class;

    EventHandler value();

    public interface NPCEventExtractor extends Function<Event, NPC> {
    }
}
