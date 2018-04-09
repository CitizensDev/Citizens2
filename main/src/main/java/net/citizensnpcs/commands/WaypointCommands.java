package net.citizensnpcs.commands;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.waypoint.Waypoints;
import net.citizensnpcs.util.Messages;

import org.bukkit.command.CommandSender;

@Requirements(ownership = true, selected = true)
public class WaypointCommands {
    public WaypointCommands(Citizens plugin) {
    }

    // TODO: refactor into a policy style system
    @Command(
            aliases = { "waypoints", "waypoint", "wp" },
            usage = "disableteleport",
            desc = "Disables teleportation when stuck (temporary command)",
            modifiers = { "disableteleport" },
            min = 1,
            max = 1,
            permission = "citizens.waypoints.disableteleport")
    public void disableTeleporting(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        npc.getNavigator().getDefaultParameters().stuckAction(null);
        Messaging.sendTr(sender, Messages.WAYPOINT_TELEPORTING_DISABLED);
    }

    @Command(
            aliases = { "waypoints", "waypoint", "wp" },
            usage = "provider [provider name] (-d)",
            desc = "Sets the current waypoint provider",
            modifiers = { "provider" },
            min = 1,
            max = 2,
            flags = "d",
            permission = "citizens.waypoints.provider")
    public void provider(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Waypoints waypoints = npc.getTrait(Waypoints.class);
        if (args.argsLength() == 1) {
            if (args.hasFlag('d')) {
                waypoints.describeProviders(sender);
            } else {
                Messaging.sendTr(sender, Messages.CURRENT_WAYPOINT_PROVIDER, waypoints.getCurrentProviderName());
            }
            return;
        }
        boolean success = waypoints.setWaypointProvider(args.getString(1));
        if (!success)
            throw new CommandException("Provider not found.");
        Messaging.sendTr(sender, Messages.WAYPOINT_PROVIDER_SET, args.getString(1));
    }
}
