package net.citizensnpcs.api.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.citizensnpcs.api.util.Messaging;

public class Injector {
    private final Class<?>[] argClasses;
    private final Object[] args;

    public Injector(Object... args) {
        this.args = args;
        argClasses = new Class[args.length];
        for (int i = 0; i < args.length; ++i) {
            argClasses[i] = args[i].getClass();
        }
    }

    public Object getInstance(Class<?> clazz) {
        try {
            Constructor<?> ctr = clazz.getConstructor(argClasses);
            ctr.setAccessible(true);
            return ctr.newInstance(args);
        } catch (NoSuchMethodException e) {
            try {
                return clazz.newInstance();
            } catch (Exception ex) {
                Messaging.severe("Error initializing commands class " + clazz + ": ");
                ex.printStackTrace();
                return null;
            }
        } catch (InvocationTargetException e) {
            Messaging.severe("Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            Messaging.severe("Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            Messaging.severe("Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
            return null;
        }
    }
}