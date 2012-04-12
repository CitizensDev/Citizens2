package net.citizensnpcs.command.command;

import java.io.File;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.scripting.CompileCallback;
import net.citizensnpcs.api.scripting.ScriptFactory;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.command.ServerCommand;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Requirements
public class AdminCommands {
    private final Citizens plugin;

    public AdminCommands(Citizens plugin) {
        this.plugin = plugin;
    }

    @Command(
             aliases = "citizens",
             modifiers = "script",
             desc = "compile and run a script",
             min = 2,
             max = 2,
             permission = "script.run")
    @ServerCommand
    public void runScript(CommandContext args, final CommandSender sender, NPC npc) throws CommandException {
        File file = new File(args.getString(1));
        if (!file.exists())
            throw new CommandException("The file '" + args.getString(1) + "' doesn't exist!");
        Messaging.send(
                sender,
                "Could put into queue? "
                        + CitizensAPI.getScriptCompiler().compile(file).withCallback(new CompileCallback() {
                            @Override
                            public void onScriptCompiled(ScriptFactory script) {
                                script.newInstance();
                                Messaging.send(sender, "<a>Script compiled!");
                            }
                        }).begin());
    }

    @Command(aliases = { "citizens" }, desc = "Show basic plugin information", max = 0, permission = "admin")
    public void citizens(CommandContext args, Player player, NPC npc) {
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
    @ServerCommand
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
    @ServerCommand
    public void save(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.send(sender, "<e>Saving Citizens...");
        plugin.save();
        Messaging.send(sender, "<e>Citizens saved.");
    }
}