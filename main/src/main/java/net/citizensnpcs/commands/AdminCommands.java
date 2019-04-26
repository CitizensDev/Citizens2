package net.citizensnpcs.commands;

import org.bukkit.ChunkSnapshot;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.StringHelper;

@Requirements
public class AdminCommands {
    private final Citizens plugin;

    public AdminCommands(Citizens plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = { "citizens" }, desc = "Show basic plugin information", max = 0, permission = "citizens.admin")
    public void citizens(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Messaging.send(sender,
                "          " + StringHelper.wrapHeader("<e>Citizens v" + plugin.getDescription().getVersion()));
        Messaging.send(sender, "     <7>-- <c>Written by fullwall and aPunch");
        Messaging.send(sender, "     <7>-- <c>Source Code: http://github.com/CitizensDev");
        Messaging.send(sender, "     <7>-- <c>Website: " + plugin.getDescription().getWebsite());
        ChunkSnapshot cs = ((Player) sender).getLocation().getChunk().getChunkSnapshot();
        for (int y = 79; y < 79 + 16; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    System.out.println(x + " " + y + " " + z + " " + cs.getBlockType(x, y, z));
                }
            }
        }
    }

    @Command(
            aliases = { "citizens" },
            usage = "reload",
            desc = "Reload Citizens",
            modifiers = { "reload" },
            min = 1,
            max = 1,
            permission = "citizens.admin")
    public void reload(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Messaging.sendTr(sender, Messages.CITIZENS_RELOADING);
        try {
            plugin.reload();
            Messaging.sendTr(sender, Messages.CITIZENS_RELOADED);
        } catch (NPCLoadException ex) {
            ex.printStackTrace();
            throw new CommandException(Messages.CITIZENS_RELOAD_ERROR);
        }
    }

    @Command(
            aliases = { "citizens" },
            usage = "save (-a)",
            desc = "Save NPCs",
            help = Messages.COMMAND_SAVE_HELP,
            modifiers = { "save" },
            min = 1,
            max = 1,
            flags = "a",
            permission = "citizens.admin")
    public void save(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.sendTr(sender, Messages.CITIZENS_SAVING);
        plugin.storeNPCs(args);
        Messaging.sendTr(sender, Messages.CITIZENS_SAVED);
    }
}