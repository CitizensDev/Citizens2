package net.citizensnpcs.command.command;

import java.util.List;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandConfigurable;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.command.exception.NoPermissionsException;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.StringHelper;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class TraitCommands {

    @Command(
            aliases = { "trait", "tr" },
            usage = "[trait name], [trait name]...",
            desc = "Adds traits to the NPC",
            modifiers = { "*" },
            min = 1,
            max = 1,
            flags = "r",
            permission = "npc.trait")
    public void add(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        List<String> added = Lists.newArrayList();
        List<String> removed = Lists.newArrayList();
        List<String> failed = Lists.newArrayList();
        for (String traitName : Splitter.on(',').split(args.getJoinedStrings(0))) {
            if (!sender.hasPermission("citizens.npc.trait." + traitName))
                failed.add(String.format("%s: No permission", traitName));

            Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(traitName);
            if (clazz == null) {
                failed.add(String.format("%s: Trait not found", traitName));
                continue;
            }
            boolean remove = npc.hasTrait(clazz);
            if (remove) {
                npc.removeTrait(clazz);
                removed.add(StringHelper.wrap(traitName));
                continue;
            }
            npc.addTrait(clazz);
            added.add(StringHelper.wrap(traitName));
        }
        if (added.size() > 0)
            Messaging
                    .sendF(sender, ChatColor.GREEN + "Added %s successfully.", Joiner.on(", ").join(removed));
        if (removed.size() > 0)
            Messaging.sendF(sender, ChatColor.GREEN + "Removed %s successfully.",
                    Joiner.on(", ").join(removed));
        if (failed.size() > 0)
            Messaging
                    .send(sender, ChatColor.GRAY + "Couldn't change the following traits - "
                            + Joiner.on(", ").join(failed));
    }

    @Command(
            aliases = { "traitc", "trc", "tc" },
            usage = "[trait name] [flags]",
            desc = "Configures a trait",
            modifiers = { "*" },
            min = 2,
            flags = "*",
            permission = "npc.trait-configure")
    public void configure(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String traitName = args.getString(0);
        if (!sender.hasPermission("citizens.npc.trait-configure." + traitName))
            throw new NoPermissionsException();
        Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(args.getString(0));
        if (clazz == null)
            throw new CommandException("Trait not found.");
        if (!clazz.isAssignableFrom(CommandConfigurable.class))
            throw new CommandException("That trait is not configurable");
        if (!npc.hasTrait(clazz))
            throw new CommandException("The NPC doesn't have that trait.");
        CommandConfigurable trait = (CommandConfigurable) npc.getTrait(clazz);
        trait.configure(args);
    }
}
