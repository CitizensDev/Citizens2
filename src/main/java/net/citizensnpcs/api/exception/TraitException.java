package net.citizensnpcs.api.exception;

/**
 * Represents an Exception thrown by traits.
 */
public class TraitException extends Exception {
    private static final long serialVersionUID = -4604062224372942561L;

    public TraitException(String msg) {
        super(msg);
    }
}