package net.citizensnpcs.command.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.CommandManager.CommandInfo;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.Paginator;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.command.CommandSender;

import com.google.common.collect.Sets;

@Requirements
public class HelpCommands {
    private final Citizens plugin;

    public HelpCommands(Citizens plugin) {
        this.plugin = plugin;
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
    public void citizensHelp(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int page = args.argsLength() == 2 ? args.getInteger(1) : 1;
        sendHelp(sender, "citizens", page);
    }

    private List<String> getLines(CommandSender sender, String baseCommand) {
        // Ensures that commands with multiple modifiers are only added once
        Set<CommandInfo> processed = Sets.newHashSet();
        List<String> lines = new ArrayList<String>();
        for (CommandInfo info : plugin.getCommands(baseCommand)) {
            Command command = info.getCommandAnnotation();
            if (processed.contains(info)
                    || (!sender.hasPermission("citizens.admin") && !sender.hasPermission("citizens."
                            + command.permission())))
                continue;
            lines.add("<7>/<c>" + command.aliases()[0]
                    + (command.usage().isEmpty() ? "" : " " + command.usage()) + " <7>- <e>"
                    + Messaging.tryTranslate(command.desc()));
            if (command.modifiers().length > 1)
                processed.add(info);
        }
        return lines;
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
    public void npcHelp(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int page = args.argsLength() == 2 ? args.getInteger(1) : 1;
        sendHelp(sender, "NPC", page);
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
    public void scriptHelp(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int page = args.argsLength() == 2 ? args.getInteger(1) : 1;
        sendHelp(sender, "script", page);
    }

    private void sendHelp(CommandSender sender, String name, int page) throws CommandException {
        Paginator paginator = new Paginator().header(StringHelper.capitalize(name)
                + Messaging.tr(Messages.COMMAND_HELP_HEADER));
        for (String line : getLines(sender, name.toLowerCase()))
            paginator.addLine(line);
        if (!paginator.sendPage(sender, page))
            throw new CommandException(Messages.COMMAND_PAGE_MISSING, page);
    }

    @Command(
            aliases = { "template", "tpl" },
            usage = "help (page)",
            desc = "Template help menu",
            modifiers = { "help" },
            min = 1,
            max = 2,
            permission = "templates.help")
    @Requirements
    public void templatesHelp(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int page = args.argsLength() == 2 ? args.getInteger(1) : 1;
        sendHelp(sender, "templates", page);
    }

    @Command(
            aliases = { "waypoint", "waypoint", "wp" },
            usage = "help (page)",
            desc = "Waypoints help menu",
            modifiers = { "help" },
            min = 1,
            max = 2,
            permission = "waypoints.help")
    @Requirements
    public void waypointsHelp(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int page = args.argsLength() == 2 ? args.getInteger(1) : 1;
        sendHelp(sender, "waypoints", page);
    }
}