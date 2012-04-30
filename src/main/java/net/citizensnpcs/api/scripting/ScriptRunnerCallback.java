package net.citizensnpcs.api.scripting;

import org.apache.commons.lang.Validate;

public class ScriptRunnerCallback implements CompileCallback {
    private final Object[] methodArgs;
    private final String methodToInvoke;

    public ScriptRunnerCallback() {
        this.methodToInvoke = null;
        this.methodArgs = null;
    }

    public ScriptRunnerCallback(String methodToInvoke) {
        this(methodToInvoke, null);
    }

    public ScriptRunnerCallback(String methodToInvoke, Object[] methodArgs) {
        Validate.notNull(methodToInvoke, "method cannot be null");
        this.methodToInvoke = methodToInvoke;
        this.methodArgs = methodArgs;
    }

    @Override
    public void onScriptCompiled(ScriptFactory factory) {
        Script script = factory.newInstance();
        invokeMethodIfAvailable(script);
    }

    private void invokeMethodIfAvailable(Script script) {
        if (methodToInvoke == null)
            return;
        script.invoke(methodToInvoke, methodArgs);
    }
}
