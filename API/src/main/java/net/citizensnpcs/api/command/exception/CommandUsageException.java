package net.citizensnpcs.api.command.exception;

public class CommandUsageException extends CommandException {
    private String usage;

    public CommandUsageException() {
        this(null, null);
    }

    public CommandUsageException(String message, String usage) {
        super(message);
        this.usage = usage;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    private static final long serialVersionUID = -6761418114414516542L;
}