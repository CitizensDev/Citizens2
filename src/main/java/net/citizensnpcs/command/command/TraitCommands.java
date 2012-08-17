package net.citizensnpcs.command.command;

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

public class TraitCommands {

    @Command(
            aliases = { "trait", "tr" },
            usage = "[trait name]",
            desc = "Adds a trait to the NPC",
            modifiers = { "*" },
            min = 1,
            max = 1,
            flags = "r",
            permission = "npc.trait")
    public void add(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        String traitName = args.getString(0);
        if (!sender.hasPermission("citizens.npc.trait." + traitName))
            throw new NoPermissionsException();

        Class<? extends Trait> clazz = CitizensAPI.getTraitFactory().getTraitClass(traitName);
        if (clazz == null)
            throw new CommandException("Trait not found.");
        boolean remove = npc.hasTrait(clazz);
        if (remove) {
            npc.removeTrait(clazz);
            Messaging.sendF(sender, ChatColor.GREEN + "Trait %s removed successfully.",
                    StringHelper.wrap(traitName));
            return;
        }
        npc.addTrait(clazz);
        Messaging.sendF(sender, ChatColor.GREEN + "Trait %s added successfully.",
                StringHelper.wrap(traitName));
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
