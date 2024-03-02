package net.citizensnpcs.commands;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.command.CommandSender;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.Settings.Setting;
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
    private final Map<CommandSender, Long> reloadTimeouts = new WeakHashMap<>();

    public AdminCommands(Citizens plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = { "citizens" }, desc = "", max = 0, permission = "citizens.admin")
    public void citizens(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Messaging.send(sender, StringHelper.wrapHeader("<green>Citizens v" + plugin.getDescription().getVersion()));
        Messaging.send(sender, "     <yellow>-- <green>Author: fullwall");
        Messaging.send(sender, "     <yellow>-- <green><click:open_url:" + plugin.getDescription().getWebsite()
                + "><hover:show_text:Citizens website including wiki><u>Website</hover></click> <click:open_url:https://discord.gg/Q6pZGSR><hover:show_text:Citizens Support Discord><u>Support</hover></click>");
    }

    @Command(
            aliases = { "citizens" },
            usage = "reload",
            desc = "",
            modifiers = { "reload", "load" },
            min = 1,
            max = 1,
            permission = "citizens.admin")
    public void reload(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (Setting.WARN_ON_RELOAD.asBoolean()) {
            Long timeout = reloadTimeouts.get(sender);
            if (timeout == null || System.currentTimeMillis() > timeout) {
                Messaging.sendErrorTr(sender, Messages.CITIZENS_RELOAD_WARNING);
                reloadTimeouts.put(sender, System.currentTimeMillis() + 5000);
                return;
            }
        }
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
            desc = "",
            help = Messages.COMMAND_SAVE_HELP,
            modifiers = { "save" },
            min = 1,
            max = 1,
            flags = "a",
            permission = "citizens.admin")
    public void save(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.sendTr(sender, Messages.CITIZENS_SAVING);
        plugin.storeNPCs(args.hasFlag('a'));
        Messaging.sendTr(sender, Messages.CITIZENS_SAVED);
    }
}