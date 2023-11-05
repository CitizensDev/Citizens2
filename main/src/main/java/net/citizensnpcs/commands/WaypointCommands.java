package net.citizensnpcs.commands;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.astar.pathfinder.ChunkBlockSource;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Flag;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.command.exception.CommandUsageException;
import net.citizensnpcs.api.hpastar.HPAGraph;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.trait.waypoint.LinearWaypointProvider;
import net.citizensnpcs.trait.waypoint.Waypoint;
import net.citizensnpcs.trait.waypoint.WaypointProvider;
import net.citizensnpcs.trait.waypoint.Waypoints;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Util;

@Requirements(ownership = true, selected = true)
public class WaypointCommands {
    @Command(
            aliases = { "waypoints", "waypoint", "wp" },
            usage = "add [x] [y] [z] (world) (--index idx)",
            desc = "Adds a waypoint at a point",
            modifiers = { "add" },
            min = 4,
            max = 5,
            permission = "citizens.waypoints.add")
    public void add(CommandContext args, CommandSender sender, NPC npc, @Flag("index") Integer index)
            throws CommandException {
        WaypointProvider provider = npc.getOrAddTrait(Waypoints.class).getCurrentProvider();
        if (!(provider instanceof LinearWaypointProvider))
            throw new CommandException();
        List<Waypoint> waypoints = (List<Waypoint>) ((LinearWaypointProvider) provider).waypoints();
        World world = args.argsLength() > 4 ? Bukkit.getWorld(args.getString(4)) : npc.getStoredLocation().getWorld();
        if (world == null)
            throw new CommandException(Messages.WORLD_NOT_FOUND);
        Location loc = new Location(world, args.getInteger(1), args.getInteger(2), args.getInteger(3));
        if (index == null) {
            index = waypoints.size();
        }
        if (index > waypoints.size() || index < 0)
            throw new CommandException("Index out of range. Can't be more than " + waypoints.size());
        waypoints.add(index, new Waypoint(loc));
        Messaging.sendTr(sender, Messages.WAYPOINT_ADDED, Util.prettyPrintLocation(loc), index);
    }

    @Command(
            aliases = { "waypoints", "waypoint", "wp" },
            usage = "disableteleport",
            desc = "Disables teleportation when stuck",
            modifiers = { "disableteleport", "dt" },
            min = 1,
            max = 1,
            permission = "citizens.waypoints.disableteleport")
    public void disableTeleporting(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        npc.data().setPersistent(NPC.Metadata.DISABLE_DEFAULT_STUCK_ACTION,
                !npc.data().get(NPC.Metadata.DISABLE_DEFAULT_STUCK_ACTION,
                        !Setting.DEFAULT_STUCK_ACTION.asString().contains("teleport")));
        if (npc.data().get(NPC.Metadata.DISABLE_DEFAULT_STUCK_ACTION,
                !Setting.DEFAULT_STUCK_ACTION.asString().contains("teleport"))) {
            npc.getNavigator().getDefaultParameters().stuckAction(null);
            Messaging.sendTr(sender, Messages.WAYPOINT_TELEPORTING_DISABLED, npc.getName());
        } else {
            npc.getNavigator().getDefaultParameters().stuckAction(TeleportStuckAction.INSTANCE);
            Messaging.sendTr(sender, Messages.WAYPOINT_TELEPORTING_ENABLED, npc.getName());
        }
    }

    @Command(
            aliases = { "waypoints", "waypoint", "wp" },
            usage = "hpa",
            desc = "Debugging command",
            modifiers = { "hpa" },
            min = 1,
            max = 1,
            permission = "citizens.waypoints.hpa")
    public void hpa(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (Messaging.isDebugging() && sender.isOp()) {
            HPAGraph graph = new HPAGraph(new ChunkBlockSource(npc.getStoredLocation(), 16),
                    npc.getStoredLocation().getBlockX(), npc.getStoredLocation().getBlockY(),
                    npc.getStoredLocation().getBlockZ());
            graph.addClusters(npc.getStoredLocation().getBlockX(), npc.getStoredLocation().getBlockZ());
            System.out.println(graph.findPath(new Location(npc.getStoredLocation().getWorld(), 8, 68, -134),
                    new Location(npc.getStoredLocation().getWorld(), 11, 68, -131)));
        }
    }

    @Command(
            aliases = { "waypoints", "waypoint", "wp" },
            usage = "opendoors",
            desc = "Enables opening doors when pathfinding",
            modifiers = { "opendoors", "od" },
            min = 1,
            max = 1,
            permission = "citizens.waypoints.opendoors")
    public void openDoors(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        boolean opensDoors = !npc.data().get(NPC.Metadata.PATHFINDER_OPEN_DOORS, false);
        npc.data().setPersistent(NPC.Metadata.PATHFINDER_OPEN_DOORS, opensDoors);
        Messaging.sendTr(sender,
                opensDoors ? Messages.PATHFINDER_OPEN_DOORS_ENABLED : Messages.PATHFINDER_OPEN_DOORS_DISABLED,
                npc.getName());
    }

    @Command(
            aliases = { "waypoints", "waypoint", "wp" },
            usage = "provider [provider name]",
            desc = "Sets the current waypoint provider",
            modifiers = { "provider" },
            min = 1,
            max = 2,
            permission = "citizens.waypoints.provider")
    public void provider(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Waypoints waypoints = npc.getOrAddTrait(Waypoints.class);
        if (args.argsLength() == 1) {
            Messaging.sendTr(sender, Messages.CURRENT_WAYPOINT_PROVIDER, waypoints.getCurrentProviderName());
            waypoints.describeProviders(sender);
            return;
        }
        if (sender instanceof Player && Editor.hasEditor((Player) sender)) {
            Editor.leave((Player) sender);
        }
        boolean success = waypoints.setWaypointProvider(args.getString(1));
        if (!success)
            throw new CommandException("Provider not found.");
        Messaging.sendTr(sender, Messages.WAYPOINT_PROVIDER_SET, args.getString(1));
    }

    @Command(
            aliases = { "waypoints", "waypoint", "wp" },
            usage = "remove (x y z world) (--index idx)",
            desc = "Adds a waypoint at a point",
            modifiers = { "remove" },
            min = 1,
            max = 5,
            permission = "citizens.waypoints.remove")
    public void remove(CommandContext args, CommandSender sender, NPC npc, @Flag("index") Integer index)
            throws CommandException {
        WaypointProvider provider = npc.getOrAddTrait(Waypoints.class).getCurrentProvider();
        if (!(provider instanceof LinearWaypointProvider))
            throw new CommandException();
        List<Waypoint> waypoints = (List<Waypoint>) ((LinearWaypointProvider) provider).waypoints();
        if (index != null && index >= 0 && index < waypoints.size()) {
            waypoints.remove((int) index);
            Messaging.sendTr(sender, Messages.WAYPOINT_REMOVED, index);
        } else {
            if (args.argsLength() < 4)
                throw new CommandUsageException();
            World world = args.argsLength() > 4 ? Bukkit.getWorld(args.getString(4))
                    : npc.getStoredLocation().getWorld();
            if (world == null)
                throw new CommandException(Messages.WORLD_NOT_FOUND);

            Location loc = new Location(world, args.getInteger(1), args.getInteger(2), args.getInteger(3));
            for (Iterator<Waypoint> iterator = waypoints.iterator(); iterator.hasNext();) {
                Waypoint wp = iterator.next();
                if (wp.getLocation().equals(loc)) {
                    iterator.remove();
                }
            }
            Messaging.sendTr(sender, Messages.WAYPOINT_REMOVED, Util.prettyPrintLocation(loc));
        }
    }
}
