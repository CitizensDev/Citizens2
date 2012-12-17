package net.citizensnpcs.command.command;

import java.util.List;

import javax.annotation.Nullable;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.Requirements;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.npc.Template;
import net.citizensnpcs.npc.Template.TemplateBuilder;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;

import org.bukkit.command.CommandSender;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Requirements(selected = true, ownership = true)
public class TemplateCommands {
    public TemplateCommands(Citizens plugin) {
    }

    @Command(
            aliases = { "template", "tpl" },
            usage = "apply [template name] (id id2...)",
            desc = "Applies a template to the selected NPC",
            modifiers = { "apply" },
            min = 2,
            permission = "templates.apply")
    @Requirements
    public void apply(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Template template = Template.byName(args.getString(1));
        if (template == null)
            throw new CommandException(Messages.TEMPLATE_MISSING);
        int appliedCount = 0;
        if (args.argsLength() == 2) {
            if (npc == null)
                throw new CommandException(Messaging.tr(Messages.COMMAND_MUST_HAVE_SELECTED));
            template.apply(npc);
            appliedCount++;
        } else {
            String joined = args.getJoinedStrings(2, ',');
            List<Integer> ids = Lists.newArrayList();
            for (String id : Splitter.on(',').trimResults().split(joined)) {
                int parsed = Integer.parseInt(id);
                ids.add(parsed);
            }
            Iterable<NPC> transformed = Iterables.transform(ids, new Function<Integer, NPC>() {
                @Override
                public NPC apply(@Nullable Integer arg0) {
                    if (arg0 == null)
                        return null;
                    return CitizensAPI.getNPCRegistry().getById(arg0);
                }
            });
            for (NPC toApply : transformed) {
                template.apply(toApply);
                appliedCount++;
            }
        }
        Messaging.sendTr(sender, Messages.TEMPLATE_APPLIED, appliedCount);
    }

    @Command(
            aliases = { "template", "tpl" },
            usage = "create [template name] (-o)",
            desc = "Creates a template from the selected NPC",
            modifiers = { "create" },
            min = 2,
            max = 2,
            flags = "o",
            permission = "templates.create")
    public void create(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String name = args.getString(1);
        if (Template.byName(name) != null)
            throw new CommandException(Messages.TEMPLATE_CONFLICT);

        TemplateBuilder.create(name).from(npc).override(args.hasFlag('o')).buildAndSave();
        Messaging.sendTr(sender, Messages.TEMPLATE_CREATED);
    }
}
