package net.citizensnpcs.api.command.exception;

public class RequirementMissingException extends CommandException {
    public RequirementMissingException(String message) {
        super(message);
    }

    private static final long serialVersionUID = -4299721983654504028L;
}