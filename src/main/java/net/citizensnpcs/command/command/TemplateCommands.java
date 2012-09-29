package net.citizensnpcs.command.command;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.npc.Template;
import net.citizensnpcs.npc.Template.TemplateBuilder;
import net.citizensnpcs.util.Messaging;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@Requirements(selected = true, ownership = true)
public class TemplateCommands {
    public TemplateCommands(Citizens plugin) {
    }

    @Command(
            aliases = { "template", "tpl" },
            usage = "apply (name)",
            desc = "Applies a template to the selected NPC",
            modifiers = { "apply" },
            min = 2,
            max = 2,
            permission = "templates.apply")
    public void apply(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Template template = Template.byName(args.getString(1));
        if (template == null)
            throw new CommandException("Template not found.");
        template.apply(npc);
        Messaging.send(sender, ChatColor.GREEN + "Template applied.");
    }

    @Command(
            aliases = { "template", "tpl" },
            usage = "create [name] (-o)",
            desc = "Creates a template from the selected NPC",
            modifiers = { "create" },
            min = 2,
            max = 2,
            flags = "o",
            permission = "templates.create")
    public void create(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String name = args.getString(1);
        if (Template.byName(name) != null)
            throw new CommandException("A template by that name already exists.");

        TemplateBuilder.create(name).from(npc).override(args.hasFlag('o')).buildAndSave();
        Messaging.send(sender, ChatColor.GREEN + "Template created.");
    }
}
