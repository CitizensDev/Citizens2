package net.citizensnpcs.api.scripting;

import java.util.concurrent.Callable;

import org.apache.commons.lang.Validate;

import com.google.common.util.concurrent.Callables;

public class ObjectProvider implements ContextProvider {
    private final String name;
    private final Callable<Object> provider;

    public ObjectProvider(String name, Object obj) {
        Validate.notNull(obj, "provided object cannot be null");
        Validate.notNull(name, "name cannot be null");
        this.name = name;
        this.provider = Callables.returning(obj);
    }

    public ObjectProvider(String name, Callable<Object> provider) {
        Validate.notNull(provider, "provider cannot be null");
        Validate.notNull(name, "name cannot be null");
        this.name = name;
        this.provider = provider;
    }

    @Override
    public void provide(Script script) {
        Object res = null;
        try {
            res = provider.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (res == null)
            return;
        script.setAttribute(name, res);
    }
}
