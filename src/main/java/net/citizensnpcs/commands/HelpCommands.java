package net.citizensnpcs.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.CommandManager.CommandInfo;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Paginator;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Sets;

@Requirements
public class HelpCommands {
    private final Citizens plugin;

    public HelpCommands(Citizens plugin) {
        this.plugin = plugin;
    }

    @Command(
            aliases = { "시티즌" },
            usage = "도움말 (페이지|명령어)",
            desc = "시티즌 도움말 매뉴",
            modifiers = { "도움말" },
            min = 1,
            max = 2,
            permission = "citizens.citizens.help")
    @Requirements
    public void citizensHelp(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int page = 1;
        try {
            page = args.argsLength() == 2 ? args.getInteger(1) : page;
        } catch (NumberFormatException e) {
            sendSpecificHelp(sender, "시티즌", args.getString(1));
        }
        sendHelp(sender, "시티즌", page);
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
            lines.add(format(command));
            if (command.modifiers().length > 1)
                processed.add(info);
        }
        return lines;
    }

    @Command(
            aliases = { "시민" },
            usage = "도움말 (페이지|명령어)",
            desc = "NPC 도움말 매뉴",
            modifiers = { "도움말" },
            min = 1,
            max = 2,
            permission = "citizens.npc.help")
    @Requirements
    public void npcHelp(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int page = 1;
        try {
            page = args.argsLength() == 2 ? args.getInteger(1) : page;
        } catch (NumberFormatException e) {
            sendSpecificHelp(sender, "시민", args.getString(1));
        }
        sendHelp(sender, "시민", page);
    }

    @Command(
            aliases = { "스크립트" },
            usage = "도움말 (페이지|명령어)",
            desc = "스크립트 도움말 매뉴",
            modifiers = { "도움말" },
            min = 1,
            max = 2,
            permission = "citizens.script.help")
    @Requirements
    public void scriptHelp(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int page = 1;
        try {
            page = args.argsLength() == 2 ? args.getInteger(1) : page;
        } catch (NumberFormatException e) {
            sendSpecificHelp(sender, "스크립트", args.getString(1));
        }
        sendHelp(sender, "스크립트", page);
    }

    private void sendHelp(CommandSender sender, String name, int page) throws CommandException {
        Paginator paginator = new Paginator().header(StringHelper.capitalize(name)
                + Messaging.tr(Messages.COMMAND_HELP_HEADER));
        for (String line : getLines(sender, name.toLowerCase()))
            paginator.addLine(line);
        if (!paginator.sendPage(sender, page))
            throw new CommandException(Messages.COMMAND_PAGE_MISSING, page);
    }

    private void sendSpecificHelp(CommandSender sender, String rootCommand, String modifier) throws CommandException {
        CommandInfo info = plugin.getCommandInfo(rootCommand, modifier);
        if (info == null)
            throw new CommandException(Messages.COMMAND_MISSING, rootCommand + " " + modifier);
        Messaging.send(sender, format(info.getCommandAnnotation()));
        String help = Messaging.tryTranslate(info.getCommandAnnotation().help());
        if (help.isEmpty())
            return;
        Messaging.send(sender, ChatColor.AQUA + help);
    }

    @Command(
            aliases = { "템플릿", "tpl" },
            usage = "도움말 (페이지|도움말)",
            desc = "템플릿 도움말 매뉴",
            modifiers = { "도움말" },
            min = 1,
            max = 2,
            permission = "citizens.templates.help")
    @Requirements
    public void templatesHelp(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int page = 1;
        try {
            page = args.argsLength() == 2 ? args.getInteger(1) : page;
        } catch (NumberFormatException e) {
            sendSpecificHelp(sender, "템플릿", args.getString(1));
        }
        sendHelp(sender, "템플릿", page);
    }

    @Command(
            aliases = { "waypoint", "waypoint", "wp" },
            usage = "help (page|command)",
            desc = "Waypoints help menu",
            modifiers = { "help" },
            min = 1,
            max = 2,
            permission = "citizens.waypoints.help")
    @Requirements
    public void waypointsHelp(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        int page = 1;
        try {
            page = args.argsLength() == 2 ? args.getInteger(1) : page;
        } catch (NumberFormatException e) {
            sendSpecificHelp(sender, "waypoints", args.getString(1));
        }
        sendHelp(sender, "waypoints", page);
    }

    private static final String COMMAND_FORMAT = "<7>/<c>%s%s <7>- <e>%s";

    private static final String format(Command command) {
        return String.format(COMMAND_FORMAT, command.aliases()[0],
                (command.usage().isEmpty() ? "" : " " + command.usage()), Messaging.tryTranslate(command.desc()));
    }
}