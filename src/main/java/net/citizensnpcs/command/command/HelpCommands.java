package net.citizensnpcs.command.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.CommandManager.CommandInfo;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.util.Paginator;

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
        Paginator paginator = new Paginator().header("Citizens Help");
        for (String line : getLines(sender, npc, "citizens"))
            paginator.addLine(line);
        if (!paginator.sendPage(sender, page))
            throw new CommandException("The page '" + page + "' does not exist.");
    }

    private List<String> getLines(CommandSender sender, NPC npc, String baseCommand) {
        // Ensures that commands with multiple modifiers are only added once
        Set<CommandInfo> processed = Sets.newHashSet();
        List<String> lines = new ArrayList<String>();
        for (CommandInfo info : plugin.getCommands(baseCommand)) {
            Command command = info.getCommandAnnotation();
            if (processed.contains(info)
                    || (!sender.hasPermission("citizens.admin") && !sender.hasPermission("citizens."
                            + command.permission())))
                continue;
            Requirements requirements = info.getRequirements();
            if (requirements != null && npc != null) {
                if (requirements.ownership() && !npc.getTrait(Owner.class).isOwnedBy(sender))
                    continue;
            }
            lines.add("<7>/<c>" + command.aliases()[0]
                    + (command.usage().isEmpty() ? "" : " " + command.usage()) + " <7>- <e>" + command.desc());
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
        Paginator paginator = new Paginator().header("NPC Help");
        for (String line : getLines(sender, npc, "npc"))
            paginator.addLine(line);
        if (!paginator.sendPage(sender, page))
            throw new CommandException("The page '" + page + "' does not exist.");
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
        Paginator paginator = new Paginator().header("Script Help");
        for (String line : getLines(sender, npc, "script"))
            paginator.addLine(line);
        if (!paginator.sendPage(sender, page))
            throw new CommandException("The page '" + page + "' does not exist.");
    }
}