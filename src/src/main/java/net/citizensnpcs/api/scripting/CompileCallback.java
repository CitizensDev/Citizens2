package net.citizensnpcs.api.scripting;

import java.io.File;

/**
 * A simple callback interface for use in {@link ScriptCompiler}.
 */
public interface CompileCallback {
    /**
     * Called when the compile task is completely finished and all scripts have
     * been compiled or rejected.
     */
    public void onCompileTaskFinished();

    /**
     * Called when a script has been compiled using the relevant script engine.
     * 
     * @param file
     *            The file that the script was compiled from
     * @param script
     *            The newly created script
     */
    public void onScriptCompiled(File file, ScriptFactory script);
}