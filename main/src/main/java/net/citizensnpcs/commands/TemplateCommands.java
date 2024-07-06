package net.citizensnpcs.commands;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;

import com.google.common.base.Joiner;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.command.Arg;
import net.citizensnpcs.api.command.Arg.CompletionsProvider;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.templates.Template;
import net.citizensnpcs.api.npc.templates.TemplateRegistry;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

@Requirements(selected = true, ownership = true)
public class TemplateCommands {
    private final TemplateRegistry registry;

    public TemplateCommands(Citizens plugin) {
        this.registry = plugin.getTemplateRegistry();
    }

    @Command(
            aliases = { "template", "tpl" },
            usage = "apply (template namespace:)[template name]",
            desc = "",
            modifiers = { "apply" },
            min = 2,
            permission = "citizens.templates.apply")
    public void apply(CommandContext args, CommandSender sender, NPC npc,
            @Arg(value = 1, completionsProvider = TemplateCompletions.class) String templateKey)
            throws CommandException {
        Template template = null;
        if (templateKey.contains(":")) {
            int idx = templateKey.indexOf(':');
            template = registry.getTemplateByKey(new NamespacedKey(templateKey.substring(0, idx),
                    templateKey.substring(idx + 1).toLowerCase(Locale.US)));
        } else {
            Collection<Template> templates = registry.getTemplates(templateKey);
            if (templates.isEmpty())
                throw new CommandException(Messages.TEMPLATE_MISSING);
            if (templates.size() > 1)
                throw new CommandException(Messages.TEMPLATE_PICKER, templateKey,
                        Joiner.on(", ").join(templates.stream().map(t -> t.getKey()).collect(Collectors.toList())));
            template = templates.iterator().next();
        }
        if (template == null)
            throw new CommandException(Messages.TEMPLATE_MISSING);

        template.apply(npc);
        Messaging.sendTr(sender, Messages.TEMPLATE_APPLIED, template.getKey().getKey(), npc.getName());
    }

    @Command(
            aliases = { "template", "tpl" },
            usage = "generate (template namespace:)[name]",
            desc = "",
            modifiers = { "generate" },
            min = 2,
            max = 2,
            permission = "citizens.templates.generate")
    public void generate(CommandContext args, CommandSender sender, NPC npc,
            @Arg(value = 1, completionsProvider = TemplateCompletions.class) String templateName)
            throws CommandException {
        int idx = templateName.indexOf(':');
        NamespacedKey key = idx == -1 ? new NamespacedKey("generated", templateName.toLowerCase(Locale.US))
                : new NamespacedKey(templateName.substring(0, idx),
                        templateName.substring(idx + 1).toLowerCase(Locale.US));
        if (registry.getTemplateByKey(key) != null)
            throw new CommandException(Messages.TEMPLATE_CONFLICT);
        registry.generateTemplateFromNPC(key, npc);
        Messaging.sendTr(sender, Messages.TEMPLATE_GENERATED, npc.getName());
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
        for (Template template : registry.getAllTemplates()) {
            Messaging.send(sender, "[[-]]    " + template.getKey());
        }
    }

    public static class TemplateCompletions implements CompletionsProvider {
        private final TemplateRegistry templateRegistry;

        public TemplateCompletions(Citizens plugin) {
            templateRegistry = plugin.getTemplateRegistry();
        }

        @Override
        public Collection<String> getCompletions(CommandContext args, CommandSender sender, NPC npc) {
            return templateRegistry.getAllTemplates().stream().map(t -> t.getKey().toString())
                    .collect(Collectors.toList());
        }
    }
}
