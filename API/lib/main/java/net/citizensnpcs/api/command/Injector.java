package net.citizensnpcs.api.command;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import net.citizensnpcs.api.util.Messaging;

public class Injector {
    private final Class<?>[] argClasses;
    private final List<Object> args;

    public Injector(Object... args) {
        this.args = Arrays.asList(args);
        argClasses = new Class[args.length];
        for (int i = 0; i < args.length; ++i) {
            argClasses[i] = args[i].getClass();
        }
    }

    public Object getInstance(Class<?> clazz) {
        try {
            return LOOKUP.findConstructor(clazz, MethodType.methodType(void.class, argClasses))
                    .invokeWithArguments(args);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            try {
                Constructor<?> ctr = clazz.getDeclaredConstructor();
                ctr.setAccessible(true);
                return ctr.newInstance();
            } catch (Exception ex) {
                Messaging.severe("Error initializing commands class " + clazz + ": ");
                ex.printStackTrace();
                return null;
            }
        } catch (Throwable e) {
            Messaging.severe("Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
            return null;
        }
    }

    private static MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
}