package net.citizensnpcs.api.scripting;

/**
 * A simple callback interface for use in {@link ScriptCompiler}.
 */
public interface CompileCallback {

    /**
     * Called when a script has been compiled using the relevant script engine.
     * 
     * @param script
     *            The newly created script
     */
    public void onScriptCompiled(ScriptFactory script);

    public void onCompileTaskFinished();
}