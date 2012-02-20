package net.citizensnpcs.api.exception;

/**
 * Represents an Exception thrown by NPCs
 */
public class NPCException extends Exception {
    private static final long serialVersionUID = -5544233658536324392L;

    public NPCException(String msg) {
        super(msg);
    }
}