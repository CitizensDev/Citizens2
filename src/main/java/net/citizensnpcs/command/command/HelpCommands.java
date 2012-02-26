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
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

@Requirements
public class HelpCommands {
    private static final int LINES_PER_PAGE = 9;

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
    public void citizensHelp(CommandContext args, Player player, NPC npc) {
        int page = args.argsLength() == 2 ? args.getInteger(1) : 1;
        if (!sendPage(player, args.getCommand(), page))
            Messaging.sendError(player, "The page '" + page + "' does not exist.");
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
    public void npcHelp(CommandContext args, Player player, NPC npc) {
        int page = args.argsLength() == 2 ? args.getInteger(1) : 1;
        if (!sendPage(player, args.getCommand(), page))
            Messaging.sendError(player, "The page '" + page + "' does not exist.");
    }

    private boolean sendPage(Player player, String baseCommand, int page) {
        List<String> lines = getLines(player, baseCommand);
        int pages = (int) ((lines.size() / LINES_PER_PAGE == 0) ? 1 : Math.ceil((double) lines.size() / LINES_PER_PAGE));
        if (page < 0 || page > pages)
            return false;

        int startIndex = LINES_PER_PAGE * page - LINES_PER_PAGE;
        int endIndex = page * LINES_PER_PAGE;

        Messaging.send(player, StringHelper.wrapHeader("<e>"
                + (baseCommand.equalsIgnoreCase("npc") ? "NPC" : StringHelper.capitalize(baseCommand.toLowerCase()))
                + " Help <f>" + page + "/" + pages));

        if (lines.size() < endIndex)
            endIndex = lines.size();
        for (String line : lines.subList(startIndex, endIndex))
            Messaging.send(player, line);
        return true;
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