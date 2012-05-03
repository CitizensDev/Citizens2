package net.citizensnpcs.api.scripting;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class SimpleScript implements Script {
    private final Bindings bindings;
    private final ScriptEngine engine;
    private final Invocable invocable;
    private final Object root;

    public SimpleScript(CompiledScript src, ContextProvider[] providers) throws ScriptException {
        this.engine = src.getEngine();
        this.invocable = (Invocable) engine;
        this.bindings = engine.createBindings();
        for (ContextProvider provider : providers)
            provider.provide(this);
        this.root = src.eval(bindings);
    }

    @Override
    public <T> T convertToInterface(Object obj, Class<T> expected) {
        if (obj == null || expected == null)
            throw new IllegalArgumentException("arguments should not be null");
        if (expected.isAssignableFrom(obj.getClass()))
            return expected.cast(obj);
        synchronized (engine) {
            Bindings old = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            T t = invocable.getInterface(expected);
            engine.setBindings(old, ScriptContext.ENGINE_SCOPE);
            return t;
        }
    }

    @Override
    public Object getAttribute(String name) {
        if (name == null)
            throw new IllegalArgumentException("name should not be null");
        return bindings.get(name);
    }

    @Override
    public Object invoke(Object instance, String name, Object... args) {
        if (instance == null || name == null)
            throw new IllegalArgumentException("instance and method name should not be null");
        try {
            synchronized (engine) {
                Bindings old = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                Object ret = invocable.invokeMethod(instance, name, args);
                engine.setBindings(old, ScriptContext.ENGINE_SCOPE);
                return ret;
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Object invoke(String name, Object... args) {
        if (name == null)
            throw new IllegalArgumentException("name should not be null");
        try {
            synchronized (engine) {
                Bindings old = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                Object ret = invocable.invokeFunction(name, args);
                engine.setBindings(old, ScriptContext.ENGINE_SCOPE);
                return ret;
            }
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (name == null || value == null)
            throw new IllegalArgumentException("arguments should not be null");
        bindings.put(name, value);
    }
}
