package net.citizensnpcs.command.exception;

public class CommandUsageException extends CommandException {
    private static final long serialVersionUID = -6761418114414516542L;

    protected String usage;

    public CommandUsageException(String message, String usage) {
        super(message);
        this.usage = usage;
    }

    public String getUsage() {
        return usage;
    }
}