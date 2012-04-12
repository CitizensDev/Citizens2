package net.citizensnpcs.api.scripting;

public interface ContextProvider {

    /**
     * Provides context to a script, such as via {@link Script#setAttribute(String, Object)}.
     * 
     * @param script
     *            The script to provide context to.
     */
    public void provide(Script script);
}