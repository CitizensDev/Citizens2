package net.citizensnpcs.api.exception;

/**
 * Thrown when an NPC fails to load properly
 */
public class NPCLoadException extends NPCException {
    private static final long serialVersionUID = -4604062224372942561L;

    public NPCLoadException(String msg) {
        super(msg);
    }
}