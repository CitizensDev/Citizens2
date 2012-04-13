package net.citizensnpcs.command.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.CommandManager;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.command.ServerCommand;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.util.Paginator;

import org.bukkit.command.CommandSender;

@Requirements
public class HelpCommands {
    private final CommandManager cmdManager;

    public HelpCommands(Citizens plugin) {
        cmdManager = plugin.getCommandManager();
    }

    @Command(
            aliases = { "citizens" },
            usage = "help (page)",
            desc = "Citizens help menu",
            modifiers = { "help" },
            min = 1,
            max = 2,
            permission = "help")
    @Requirements
    @ServerCommand
    public void citizensHelp(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int page = args.argsLength() == 2 ? args.getInteger(1) : 1;
        Paginator paginator = new Paginator().header("Citizens Help");
        for (String line : getLines(sender, "citizens"))
            paginator.addLine(line);
        if (!paginator.sendPage(sender, page))
            throw new CommandException("The page '" + page + "' does not exist.");
    }

    private List<String> getLines(CommandSender sender, String baseCommand) {
        // Ensures that commands with multiple modifiers are only added once
        Set<Command> cmds = new HashSet<Command>();
        List<String> lines = new ArrayList<String>();
        for (Command cmd : cmdManager.getCommands(baseCommand)) {
            if (cmds.contains(cmd)
                    || (!sender.hasPermission("citizens.admin") && !sender
                            .hasPermission("citizens." + cmd.permission())))
                continue;

            lines.add("<7>/<c>" + cmd.aliases()[0] + (cmd.usage().isEmpty() ? "" : " " + cmd.usage()) + " <7>- <e>"
                    + cmd.desc());
            if (cmd.modifiers().length > 1)
                cmds.add(cmd);
        }
        return lines;
    }

    @Command(
            aliases = { "script" },
            usage = "help (page)",
            desc = "Script help menu",
            modifiers = { "help" },
            min = 1,
            max = 2,
            permission = "script.help")
    @Requirements
    @ServerCommand
    public void scriptHelp(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int page = args.argsLength() == 2 ? args.getInteger(1) : 1;
        Paginator paginator = new Paginator().header("Script Help");
        for (String line : getLines(sender, "script"))
            paginator.addLine(line);
        if (!paginator.sendPage(sender, page))
            throw new CommandException("The page '" + page + "' does not exist.");
    }

    @Command(
            aliases = { "npc" },
            usage = "help (page)",
            desc = "NPC help menu",
            modifiers = { "help" },
            min = 1,
            max = 2,
            permission = "npc.help")
    @Requirements
    @ServerCommand
    public void npcHelp(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int page = args.argsLength() == 2 ? args.getInteger(1) : 1;
        Paginator paginator = new Paginator().header("NPC Help");
        for (String line : getLines(sender, "npc"))
            paginator.addLine(line);
        if (!paginator.sendPage(sender, page))
            throw new CommandException("The page '" + page + "' does not exist.");
    }
}