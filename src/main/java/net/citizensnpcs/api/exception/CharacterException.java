package net.citizensnpcs.api.exception;

/**
 * Represents an Exception thrown by characters.
 */
public class CharacterException extends Exception {
    private static final long serialVersionUID = -4604062224372942561L;

    public CharacterException(String msg) {
        super(msg);
    }
}