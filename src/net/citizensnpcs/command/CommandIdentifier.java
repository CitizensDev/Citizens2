package net.citizensnpcs.command;

import com.google.common.base.Objects;

public class CommandIdentifier {
    private final String modifier;
    private final String command;

    public CommandIdentifier(String command, String modifier) {
        this.command = command;
        this.modifier = modifier;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(command, modifier);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CommandIdentifier other = (CommandIdentifier) obj;
        if (command == null) {
            if (other.command != null)
                return false;
        } else if (!command.equals(other.command))
            return false;
        if (modifier == null) {
            if (other.modifier != null)
                return false;
        } else if (!modifier.equals(other.modifier))
            return false;
        return true;
    }

    public String getModifier() {
        return modifier;
    }

    public String getCommand() {
        return command;
    }
}