package net.citizensnpcs.api.trait;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitEventHandler.NPCEventExtractor;
import net.citizensnpcs.api.util.Messaging;

/**
 * Builds a trait.
 */
public final class TraitInfo {
    private boolean defaultTrait;
    private String name;
    private Supplier<? extends Trait> supplier;
    private boolean trackStats;
    private final Class<? extends Trait> trait;

    private TraitInfo(Class<? extends Trait> trait) {
        this.trait = trait;
        TraitName anno = trait.getAnnotation(TraitName.class);
        if (anno != null) {
            name = anno.value().toLowerCase(Locale.ROOT);
        }
        try {
            Constructor<? extends Trait> cons = trait.getDeclaredConstructor();
            cons.setAccessible(true);
            supplier = () -> {
                try {
                    return cons.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            };
        } catch (NoSuchMethodException | SecurityException e) {
        }
    }

    public TraitInfo asDefaultTrait() {
        this.defaultTrait = true;
        return this;
    }

    public void checkValid() {
        if (supplier == null) {
            try {
                trait.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Trait class must have a no-arguments constructor");
            }
        }
    }

    public Class<? extends Trait> getTraitClass() {
        return trait;
    }

    public String getTraitName() {
        return name;
    }

    public boolean isDefaultTrait() {
        return defaultTrait;
    }

    public TraitInfo optInToStats() {
        this.trackStats = true;
        return this;
    }

    public void registerListener(Plugin plugin) {
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        for (Method method : trait.getDeclaredMethods()) {
            TraitEventHandler sel = method.getAnnotation(TraitEventHandler.class);
            if (sel == null)
                continue;
            final NPCEventExtractor processor;
            try {
                processor = sel.processor() != NPCEventExtractor.class
                        ? sel.processor().getDeclaredConstructor().newInstance()
                        : event -> {
                            if (event instanceof NPCEvent) {
                                return ((NPCEvent) event).getNPC();
                            } else if (event instanceof EntityEvent) {
                                return CitizensAPI.getNPCRegistry().getNPC(((EntityEvent) event).getEntity());
                            }
                            return null;
                        };
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
            try {
                HandlerList handler = null;
                for (Field field : eventClass.getDeclaredFields()) {
                    if (field.getType() == HandlerList.class && Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        handler = (HandlerList) field.get(null);
                    }
                }
                if (handler == null) {
                    Messaging.severe("Can't get handlerlist for event " + eventClass);
                    continue;
                }
                method.setAccessible(true);
                final MethodHandle asMethodHandle = lookup.unreflect(method);
                handler.register(new RegisteredListener(new Listener() {
                }, (Listener listener, Event event) -> {
                    NPC npc = processor.apply(event);
                    if (npc == null)
                        return;
                    Trait instance = npc.getTraitNullable(trait);
                    if (instance == null)
                        return;
                    try {
                        asMethodHandle.invoke(instance, event);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }, sel.value().priority(), plugin, sel.value().ignoreCancelled()));
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    public boolean shouldTrackStats() {
        return trackStats;
    }

    @SuppressWarnings("unchecked")
    public <T extends Trait> T tryCreateInstance() {
        return (T) supplier.get();
    }

    public TraitInfo withName(String name) {
        Objects.requireNonNull(name);
        this.name = name.toLowerCase(Locale.ROOT);
        return this;
    }

    public TraitInfo withSupplier(Supplier<? extends Trait> supplier) {
        this.supplier = supplier;
        return this;
    }

    /**
     * Constructs a factory with the given trait class. The trait class must have a no-arguments constructor.
     *
     * @param trait
     *            Class of the trait
     * @return The created {@link TraitInfo}
     * @throws IllegalArgumentException
     *             If the trait class does not have a no-arguments constructor
     */
    public static TraitInfo create(Class<? extends Trait> trait) {
        Objects.requireNonNull(trait);
        return new TraitInfo(trait);
    }
}