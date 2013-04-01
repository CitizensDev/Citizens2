package net.citizensnpcs.api.scripting;

import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Callables;

public class ObjectProvider implements ContextProvider {
    private final String name;
    private final Callable<Object> provider;

    public ObjectProvider(String name, Callable<Object> provider) {
        Preconditions.checkNotNull(provider, "provider cannot be null");
        Preconditions.checkNotNull(name, "name cannot be null");
        this.name = name;
        this.provider = provider;
    }

    public ObjectProvider(String name, Object obj) {
        Preconditions.checkNotNull(obj, "provided object cannot be null");
        Preconditions.checkNotNull(name, "name cannot be null");
        this.name = name;
        this.provider = Callables.returning(obj);
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
