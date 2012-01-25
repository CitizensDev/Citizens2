// $Id$
/*
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
 */

package net.citizensnpcs.command;

import java.lang.reflect.InvocationTargetException;

public interface Injector {

    public Object getInstance(Class<?> cls) throws InvocationTargetException, IllegalAccessException,
            InstantiationException;
}