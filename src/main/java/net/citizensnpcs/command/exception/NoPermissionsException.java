package net.citizensnpcs.command.exception;

public class NoPermissionsException extends CommandException {
    public NoPermissionsException() {
        super("You don't have permission to execute that command.");
    }

    private static final long serialVersionUID = -602374621030168291L;
}