package net.citizensnpcs.api.exception;

/**
 * Represents an Exception thrown by NPCs.
 */
public abstract class NPCException extends Exception {
    public NPCException(String msg) {
        super(msg);
    }

    private static final long serialVersionUID = -5544233658536324392L;
}