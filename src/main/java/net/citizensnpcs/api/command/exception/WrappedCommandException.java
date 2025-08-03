package net.citizensnpcs.api.command.exception;

public class WrappedCommandException extends CommandException {
    public WrappedCommandException(Throwable t) {
        super(t);
    }

    private static final long serialVersionUID = -4075721444847778918L;
}