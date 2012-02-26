package net.citizensnpcs.command.exception;

public class CommandException extends Exception {
    private static final long serialVersionUID = 870638193072101739L;

    public CommandException() {
        super();
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(Throwable t) {
        super(t);
    }
}