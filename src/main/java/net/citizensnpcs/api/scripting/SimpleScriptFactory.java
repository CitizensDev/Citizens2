package net.citizensnpcs.api.scripting;

import javax.script.CompiledScript;
import javax.script.ScriptException;

import com.google.common.base.Throwables;

public class SimpleScriptFactory implements ScriptFactory {
    private final ContextProvider[] providers;
    private final CompiledScript src;

    SimpleScriptFactory(CompiledScript src, ContextProvider... providers) {
        if (src == null)
            throw new IllegalArgumentException("src cannot be null");
        if (providers == null)
            providers = new ContextProvider[0];
        this.src = src;
        this.providers = providers;
    }

    @Override
    public Script newInstance() {
        try {
            return new SimpleScript(src, providers);
        } catch (ScriptException e) {
            Throwables.getRootCause(e).printStackTrace();
            return null;
        }
    }
}