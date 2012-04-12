package net.citizensnpcs.api.scripting;

/**
 * Represents a producer of {@link Script}s.
 */
public interface ScriptFactory {

    /**
     * Creates a new local context of the {@link Script}.
     * 
     * @return The new Script.
     */
    Script newInstance();
}