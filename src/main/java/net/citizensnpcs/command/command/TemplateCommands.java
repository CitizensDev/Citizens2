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
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.ChatColor;
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
            usage = "apply [name] (id id2...)",
            desc = "Applies a template to the selected NPC",
            modifiers = { "apply" },
            min = 2,
            permission = "templates.apply")
    public void apply(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Template template = Template.byName(args.getString(1));
        if (template == null)
            throw new CommandException("Template not found.");
        int appliedCount = 0;
        if (args.argsLength() == 2) {
            template.apply(npc);
            appliedCount++;
        } else {
            String joined = args.getJoinedStrings(2, ',');
            List<Integer> j = Lists.newArrayList();
            for (String id : Splitter.on(',').trimResults().split(joined)) {
                int parsed = Integer.parseInt(id);
                j.add(parsed);
            }
            Iterable<NPC> transformed = Iterables.transform(j, new Function<Integer, NPC>() {
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
        Messaging.sendF(sender, ChatColor.GREEN + "Template applied to %s NPCs.",
                StringHelper.wrap(appliedCount));
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
