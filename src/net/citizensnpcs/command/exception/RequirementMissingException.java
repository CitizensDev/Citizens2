package net.citizensnpcs.command.exception;

public class RequirementMissingException extends CommandException {
    private static final long serialVersionUID = -4299721983654504028L;

    public RequirementMissingException(String message) {
        super(message);
    }
}