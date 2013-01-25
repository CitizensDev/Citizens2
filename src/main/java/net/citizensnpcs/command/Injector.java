package net.citizensnpcs.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import net.citizensnpcs.api.util.Messaging;

@Deprecated
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
                Messaging.log(Level.SEVERE, "Error initializing commands class " + clazz + ": ");
                ex.printStackTrace();
                return null;
            }
        } catch (InvocationTargetException e) {
            Messaging.log(Level.SEVERE, "Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
            return null;
        } catch (InstantiationException e) {
            Messaging.log(Level.SEVERE, "Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            Messaging.log(Level.SEVERE, "Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
            return null;
        }
    }
}