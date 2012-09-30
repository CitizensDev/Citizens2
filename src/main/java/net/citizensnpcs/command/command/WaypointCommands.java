package net.citizensnpcs.command.command;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.trait.waypoint.Waypoints;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@Requirements(ownership = true, selected = true)
public class WaypointCommands {
    public WaypointCommands(Citizens plugin) {
    }

    @Command(
            aliases = { "waypoints", "waypoint", "wp" },
            usage = "provider (provider name) (-a)",
            desc = "Sets the current waypoint provider",
            modifiers = { "provider" },
            min = 1,
            max = 2,
            permission = "waypoints.provider",
            traits = Waypoints.class)
    public void provider(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Waypoints waypoints = npc.getTrait(Waypoints.class);
        if (args.argsLength() == 1) {
            if (args.hasFlag('a')) {
                waypoints.describeProviders(sender);
            } else
                Messaging.sendTr(sender, ChatColor.GREEN, Messages.CURRENT_WAYPOINT_PROVIDER,
                        StringHelper.wrap(waypoints.getCurrentProviderName()));
            return;
        }
        boolean success = waypoints.setWaypointProvider(args.getString(1));
        if (!success)
            throw new CommandException("Provider not found.");
        Messaging.sendTr(sender, ChatColor.GREEN, Messages.WAYPOINT_PROVIDER_SET,
                StringHelper.wrap(args.getString(1)));
    }
}
