package net.citizensnpcs.commands;

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

import org.bukkit.command.CommandSender;

@Requirements
public class AdminCommands {
    private final Citizens plugin;

    public AdminCommands(Citizens plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = { "시티즌" }, desc = "플러그인 기본 정보를 보여줍니다", max = 0, permission = "citizens.admin")
    public void citizens(CommandContext args, CommandSender sender, NPC npc) {
        Messaging.send(sender,
                "          " + StringHelper.wrapHeader("<e>시티즌 v" + plugin.getDescription().getVersion()));
        Messaging.send(sender, "     <7>-- <c>제작자: fullwall and aPunch");
        Messaging.send(sender, "     <7>-- <c>소스코드: http://github.com/CitizensDev");
        Messaging.send(sender, "     <7>-- <c>웹사이트: " + plugin.getDescription().getWebsite());
        Messaging.send(sender, "     <7>-- <c>한글화: wolfwork");
    }

    @Command(
            aliases = { "시티즌" },
            usage = "리로드",
            desc = "시티즌을 리로드합니다",
            modifiers = { "리로드" },
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
            aliases = { "시티즌" },
            usage = "저장 (-a)",
            desc = " NPC들을 저장합니다",
            help = Messages.COMMAND_SAVE_HELP,
            modifiers = { "저장" },
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