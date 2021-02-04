package net.citizensnpcs.api.command;

import net.citizensnpcs.api.command.exception.CommandException;

public interface CommandConfigurable {
    void configure(CommandContext args) throws CommandException;
}
