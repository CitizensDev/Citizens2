package net.citizensnpcs.api.command.exception;

public class CommandUsageException extends CommandException {
    protected String usage;

    public CommandUsageException(String message, String usage) {
        super(message);
        this.usage = usage;
    }

    public String getUsage() {
        return usage;
    }

    private static final long serialVersionUID = -6761418114414516542L;
}