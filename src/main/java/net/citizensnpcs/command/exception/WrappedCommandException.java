package net.citizensnpcs.command.exception;

public class WrappedCommandException extends CommandException {
    private static final long serialVersionUID = -4075721444847778918L;

    public WrappedCommandException(Throwable t) {
        super(t);
    }
}