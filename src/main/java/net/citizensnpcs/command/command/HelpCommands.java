package net.citizensnpcs.command.command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.CommandManager;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.util.Paginator;

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
    public void citizensHelp(CommandContext args, Player player, NPC npc) throws CommandException {
        int page = args.argsLength() == 2 ? args.getInteger(1) : 1;
        Paginator paginator = new Paginator();
        for (String line : getLines(player, "citizens"))
            paginator.addLine(line);
        paginator.setHeaderText("Citizens Help");
        if (!paginator.sendPage(player, page))
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
    public void npcHelp(CommandContext args, Player player, NPC npc) throws CommandException {
        int page = args.argsLength() == 2 ? args.getInteger(1) : 1;
        Paginator paginator = new Paginator();
        for (String line : getLines(player, "npc"))
            paginator.addLine(line);
        paginator.setHeaderText("NPC Help");
        if (!paginator.sendPage(player, page))
            throw new CommandException("The page '" + page + "' does not exist.");
    }

    private List<String> getLines(Player player, String baseCommand) {
        // Ensures that commands with multiple modifiers are only added once
        Set<Command> cmds = new HashSet<Command>();
        List<String> lines = new ArrayList<String>();
        for (Command cmd : cmdManager.getCommands(baseCommand)) {
            if (cmds.contains(cmd)
                    || (!player.hasPermission("citizens.admin") && !player
                            .hasPermission("citizens." + cmd.permission())))
                continue;

            lines.add("<7>/<c>" + cmd.aliases()[0] + (cmd.usage().isEmpty() ? "" : " " + cmd.usage()) + " <7>- <e>"
                    + cmd.desc());
            if (cmd.modifiers().length > 1)
                cmds.add(cmd);
        }
        return lines;
    }
}