package net.citizensnpcs.api.command.exception;

import net.citizensnpcs.api.command.CommandMessages;

public class NoPermissionsException extends CommandException {
    public NoPermissionsException() {
        super(CommandMessages.NO_PERMISSION);
    }

    private static final long serialVersionUID = -602374621030168291L;
}