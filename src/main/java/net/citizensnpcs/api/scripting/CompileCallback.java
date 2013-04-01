package net.citizensnpcs.api.scripting;

public interface CompileCallback {
    /**
     * Called when the {@link ScriptFactory} has been compiled successfully.
     * Note that this may be called <em>in another thread</em> - make sure your
     * handling code is threadsafe.
     * 
     * @param sourceDescriptor
     *            A source description: may be a file name or a unique
     *            identifier assigned to a source string, or null.
     * @param compiled
     *            The compiled source code
     */
    public void onScriptCompiled(String sourceDescriptor, ScriptFactory compiled);
}
