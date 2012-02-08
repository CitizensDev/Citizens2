package net.citizensnpcs.command.command;

import org.bukkit.entity.Player;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.annotation.Command;
import net.citizensnpcs.command.annotation.Requirements;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

@Requirements
public class AdminCommands {
    private final Citizens plugin;

    public AdminCommands(Citizens plugin) {
        this.plugin = plugin;
    }

    @Command(
             aliases = { "citizens" },
             desc = "Shows basic plugin information",
             max = 0,
             permission = "admin")
    @Requirements
    public void citizens(CommandContext args, Player player, NPC npc) {
        Messaging.send(player, "          " + StringHelper.wrapHeader("<e>Citizens v" + plugin.getDescription().getVersion()));
        Messaging.send(player, "     <7>-- <c>Written by fullwall and aPunch");
        Messaging.send(player, "     <7>-- <c>Source: http://github.com/CitizensDev");
        Messaging.send(player, "     <7>-- <c>Website: " + plugin.getDescription().getWebsite());
    }
}