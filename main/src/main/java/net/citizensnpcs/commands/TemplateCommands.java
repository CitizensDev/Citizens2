package net.citizensnpcs.commands;

import java.io.IOException;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.CommandMessages;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.Template;
import net.citizensnpcs.util.Messages;

@Requirements(selected = true, ownership = true)
public class TemplateCommands {
    public TemplateCommands(Citizens plugin) {
    }

    @Command(
            aliases = { "template", "tpl" },
            usage = "apply [template name] (id id2...)",
            desc = "",
            modifiers = { "apply" },
            min = 2,
            permission = "citizens.templates.apply")
    @Requirements
    public void apply(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Template template = Template.byName(args.getString(1));
        if (template == null)
            throw new CommandException(Messages.TEMPLATE_MISSING);
        int appliedCount = 0;
        if (args.argsLength() == 2) {
            if (npc == null)
                throw new CommandException(Messaging.tr(CommandMessages.MUST_HAVE_SELECTED));
            template.apply(npc);
            appliedCount++;
        } else {
            String joined = args.getJoinedStrings(2, ',');
            List<Integer> ids = Lists.newArrayList();
            for (String id : Splitter.on(',').trimResults().split(joined)) {
                ids.add(Ints.tryParse(id));
            }
            Iterable<NPC> transformed = Iterables.transform(ids, id -> CitizensAPI.getNPCRegistry().getById(id));
            for (NPC toApply : transformed) {
                template.apply(toApply);
                appliedCount++;
            }
        }
        Messaging.sendTr(sender, Messages.TEMPLATE_APPLIED, appliedCount);
    }

    @Command(
            aliases = { "template", "tpl" },
            usage = "create [template name] (-k)",
            desc = "",
            modifiers = { "create" },
            min = 2,
            max = 2,
            flags = "o",
            permission = "citizens.templates.create")
    public void create(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String name = args.getString(1);
        if (Template.byName(name) != null)
            throw new CommandException(Messages.TEMPLATE_CONFLICT);

        try {
            Template.Builder.create(name).from(npc).override(!args.hasFlag('k')).buildAndSave();
            Messaging.sendTr(sender, Messages.TEMPLATE_CREATED);
        } catch (IOException e) {
            Messaging.sendError(sender, "Invalid template filename");
        }
    }

    @Command(
            aliases = { "template", "tpl" },
            usage = "delete [template name]",
            desc = "",
            modifiers = { "delete" },
            min = 2,
            max = 2,
            permission = "citizens.templates.delete")
    public void delete(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String name = args.getString(1);
        if (Template.byName(name) == null)
            throw new CommandException(Messages.TEMPLATE_MISSING);
        Template.byName(name).delete();
        Messaging.sendTr(sender, Messages.TEMPLATE_DELETED, name);
    }

    @Command(
            aliases = { "template", "tpl" },
            usage = "list",
            desc = "",
            modifiers = { "list" },
            min = 1,
            max = 1,
            permission = "citizens.templates.list")
    public void list(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Messaging.sendTr(sender, Messages.TEMPLATE_LIST_HEADER);
        for (Template template : Template.getTemplates()) {
            Messaging.send(sender, "[[-]]    " + template.getName());
        }
    }
}
