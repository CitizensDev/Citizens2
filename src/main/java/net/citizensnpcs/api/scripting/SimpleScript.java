package net.citizensnpcs.api.scripting;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptException;

public class SimpleScript implements Script {
    private final Bindings bindings;
    private final Invocable invocable;
    private final Object root;

    public SimpleScript(CompiledScript src) throws ScriptException {
        this.invocable = (Invocable) src.getEngine();
        this.bindings = src.getEngine().createBindings();
        this.root = src.eval(bindings);
    }

    @Override
    public Object getAttribute(String name) {
        if (name == null)
            throw new IllegalArgumentException("name should not be null");
        return bindings.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (name == null || value == null)
            throw new IllegalArgumentException("arguments should not be null");
        bindings.put(name, value);
    }

    @Override
    public Object invoke(String name, Object... args) throws NoSuchMethodException {
        if (name == null)
            throw new IllegalArgumentException("name should not be null");
        try {
            return invocable.invokeFunction(name, args);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object invoke(Object instance, String name, Object... args) throws NoSuchMethodException {
        if (instance == null || name == null)
            throw new IllegalArgumentException("instance and method name should not be null");
        try {
            return invocable.invokeMethod(instance, name, args);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T asInterface(Object obj, Class<T> expected) {
        if (obj == null || expected == null)
            throw new IllegalArgumentException("arguments should not be null");
        if (expected.isAssignableFrom(obj.getClass()))
            return expected.cast(obj);
        return invocable.getInterface(expected);
    }

}
