// $Id$
/*
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
 */

package net.citizensnpcs.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import net.citizensnpcs.util.Messaging;

public class Injector {

    private Object[] args;
    private Class<?>[] argClasses;

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
            Messaging.log(Level.SEVERE, "Error initializing commands class " + clazz + ": ");
            e.printStackTrace();
            return null;
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