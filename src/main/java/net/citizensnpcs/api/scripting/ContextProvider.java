package net.citizensnpcs.api.scripting;

/**
 * Provides useful objects or methods to an instance of {@link Script}. It
 * should be run just before the script is evaluated, to ensure that the root
 * level of the script can access the provided functions.
 */
public interface ContextProvider {

    /**
     * Provides context to a script, such as via {@link Script#setAttribute(String, Object)}.
     * 
     * @param script
     *            The script to provide context to.
     */
    public void provide(Script script);
}
