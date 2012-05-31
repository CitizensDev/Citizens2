package net.citizensnpcs.command.command;

import net.citizensnpcs.api.abstraction.CommandSender;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.bukkit.CitizensBukkit;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

@Requirements
public class AdminCommands {
    private final CitizensBukkit plugin;

    public AdminCommands(CitizensBukkit plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = { "citizens" }, desc = "Show basic plugin information", max = 0, permission = "admin")
    public void citizens(CommandContext args, CommandSender player, NPC npc) {
        Messaging.send(player,
                "          " + StringHelper.wrapHeader("<e>Citizens v" + plugin.getDescription().getVersion()));
        Messaging.send(player, "     <7>-- <c>Written by fullwall and aPunch");
        Messaging.send(player, "     <7>-- <c>Source: http://github.com/CitizensDev");
        Messaging.send(player, "     <7>-- <c>Website: " + plugin.getDescription().getWebsite());
    }

    @Command(
            aliases = { "citizens" },
            usage = "reload",
            desc = "Reload Citizens",
            modifiers = { "reload" },
            min = 1,
            max = 1,
            permission = "admin")
    public void reload(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Messaging.send(sender, "<e>Reloading Citizens...");
        try {
            plugin.reload();
            Messaging.send(sender, "<e>Citizens reloaded.");
        } catch (NPCLoadException ex) {
            ex.printStackTrace();
            throw new CommandException("Error occured while reloading, see console.");
        }
    }

    @Command(
            aliases = { "citizens" },
            usage = "save",
            desc = "Save NPCs",
            modifiers = { "save" },
            min = 1,
            max = 1,
            permission = "admin")
    public void save(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.send(sender, "<e>Saving Citizens...");
        plugin.save();
        Messaging.send(sender, "<e>Citizens saved.");
    }
}