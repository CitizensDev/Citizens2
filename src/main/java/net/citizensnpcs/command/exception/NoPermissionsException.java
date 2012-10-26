package net.citizensnpcs.command.exception;

import net.citizensnpcs.util.Messages;

public class NoPermissionsException extends CommandException {
    public NoPermissionsException() {
        super(Messages.COMMAND_NO_PERMISSION);
    }

    private static final long serialVersionUID = -602374621030168291L;
}